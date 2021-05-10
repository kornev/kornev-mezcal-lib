package com.ensime.mezcal

import java.nio.file.Files

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

import com.datastax.driver.core.{ Session, Cluster => Driver, ConsistencyLevel }
import com.github.nosan.embedded.cassandra.{
  Cassandra,
  CassandraBuilder,
  Settings,
  WorkingDirectoryDestroyer
}
import org.scalatest.{ BeforeAndAfterAll, Inside }
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.should.Matchers
import org.slf4j.LoggerFactory

import com.ensime.mezcal.cluster.ClusterManager
import com.ensime.mezcal.cql._
import com.ensime.mezcal.entities.User
import com.ensime.mezcal.tracing.ExecutionInfo
import com.ensime.mezcal.util._

class QueryAsyncSpec
  extends AnyFreeSpec
     with Matchers
     with Inside
     with ScalaFutures
     with BeforeAndAfterAll {

  private[this] val log = LoggerFactory.getLogger(getClass.getName.stripSuffix("$"))

  private[this] var _cassandra: Cassandra = _
  private[this] var _driver: Driver       = _

  lazy val settings: Settings        = _cassandra.getSettings
  implicit lazy val session: Session = _driver.connect()

  protected override def beforeAll(): Unit = {
    _cassandra = new CassandraBuilder()
      .workingDirectory(() => Files.createTempDirectory("embedded-cassandra-"))
      .workingDirectoryDestroyer(WorkingDirectoryDestroyer.deleteAll())
      .addConfigProperty("authenticator", "PasswordAuthenticator")
      .addConfigProperty("authorizer", "CassandraAuthorizer")
      .addSystemProperty("cassandra.superuser_setup_delay_ms", 0)
      .version("3.11.10")
      .build
    _cassandra.start()
    _driver = {
      val node = (
        settings.getAddress.getHostAddress,
        settings.getPort.toInt,
        "cassandra",
        "cassandra"
      )
      ClusterManager.make(List(node))
    }
    session.execute(
      "CREATE KEYSPACE employee WITH replication = {'class':'SimpleStrategy','replication_factor' : 1}"
    )
    session.execute(
      "CREATE TABLE employee.profiles (id INT PRIMARY KEY, name VARCHAR, job VARCHAR)"
    )
  }

  "Проверка возможностей асинхронного api:" - {
    "добавдение нового элемента" in {
      val addUser: (Tuple3[Int, String, String]) => Future[Boolean] =
        cql"INSERT INTO employee.profiles (id, name, job) VALUES (?, ?, ?) IF NOT EXISTS"
          .insertOne[Tuple3[Int, String, String]](
            idempotent = true,
            tracing = ExecutionInfo.ENABLE
          )

      whenReady(addUser(1, "Mäkinen", "Miner")) { applied => applied shouldBe true }
    }
    "получение конкретного элемента" in {
      val getUser: (Tuple1[Int]) => Future[User] =
        cql"SELECT * FROM employee.profiles WHERE id = ?"
          .selectOne[Tuple1[Int], User](
            level = ConsistencyLevel.ONE,
            tracing = ExecutionInfo.ENABLE
          )

      whenReady(getUser(Tuple1(1))) { user =>
        inside(user) { case User(id, _, _) => id shouldBe 1 }
      }
    }
    "добавдение нескольких элементов" in {
      val oneQuery =
        cql"INSERT INTO employee.profiles (id, name, job) VALUES (?, ?, ?)"

      val twoQuery = // "Re-preparing already prepared query", так как запрос уже подготовлен в первом тесте.
        cql"INSERT INTO employee.profiles (id, name, job) VALUES (?, ?, ?) IF NOT EXISTS"

      val addTwoUsers = logged(oneQuery, twoQuery)
        .batch[Tuple2[Tuple3[Int, String, String], Tuple3[Int, String, String]]](
          idempotent = true,
          level = ConsistencyLevel.ONE,
          tracing = ExecutionInfo.ENABLE
        )

      whenReady(addTwoUsers((1, "Mäkinen", "Miner"), (1, "Mäkinen", "Miner"))) { applied =>
        applied shouldBe false
      }
    }
    "получение всех элементов" in {
      val getAllUsers: (Tuple0) => Future[Iterator[User]] =
        cql"SELECT * FROM employee.profiles"
          .selectAll[Tuple0, User](
            fetchSize = 1,
            level = ConsistencyLevel.ONE,
            tracing = ExecutionInfo.ENABLE
          )

      whenReady(getAllUsers(Tuple0())) { users =>
        users shouldBe a[Iterator[_]]
        users should have size 1
      }
    }
  }

  protected override def afterAll(): Unit = {
    session.close()
    _driver.close()
    _cassandra.stop()
  }
}
