.PHONY: all clean publish test style coverage
.SILENT:    clean publish test style coverage

.DEFAULT: all

all: clean test

clean:
	if [ -d "target"          ]; then rm --recursive target;          fi
	if [ -d "project/target"  ]; then rm --recursive project/target;  fi
	if [ -d "project/project" ]; then rm --recursive project/project; fi

publish:
	cs launch --jvm adopt:1.8.0-262 sbt -- publishSigned

release:
	cs launch --jvm adopt:1.8.0-262 sbt -- sonatypeBundleRelease

test:
	cs launch --jvm adopt:1.8.0-262 sbt -- test

style:
	cs launch --jvm adopt:1.8.0-262 sbt -- scalafix scalafmt

coverage:
	cs launch --jvm adopt:1.8.0-262 sbt -- "set coverageEnabled := true" test coverageReport
