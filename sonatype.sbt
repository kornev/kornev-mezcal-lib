homepage := Some(url("https://github.com/kornev/kornev-mezcal-lib"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/kornev/kornev-mezcal-lib"),
    "git@github.com:kornev/kornev-mezcal-lib.git"
  )
)
developers := List(
  Developer("kornev", "Vadim Kornev", "kornev@zoho.com", url("https://github.com/kornev"))
)
licenses += ("MIT", url("http://opensource.org/licenses/MIT"))
publishMavenStyle := true
