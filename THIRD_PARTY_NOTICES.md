# Third-party notices

Original MarketFlow source is MIT licensed. That license does not relicense dependencies.

- **MySQL Connector/J 26.10.0:** GPL v2 with Universal FOSS Exception; official source and license: https://github.com/mysql/mysql-connector-j and https://www.mysql.com/about/legal/licensing/foss-exception/. The original MIT project is distributed under the applicable exception; retain vendor notices. The shaded JAR includes the connector's root LICENSE and INFO_BIN/INFO_SRC notices. Corresponding dependency source JARs are bundled under dist/dependency-sources/.
- **Protocol Buffers Java 4.36.1:** transitive Connector/J dependency; BSD 3-Clause license: https://github.com/protocolbuffers/protobuf/blob/main/LICENSE. Its license notice is included alongside the distributable JAR in dist/THIRD_PARTY-LICENSES.txt.
- **JUnit Jupiter 5.11.4:** EPL 2.0, test-only: https://github.com/junit-team/junit5/blob/main/LICENSE.md.
- **MySQL Server Docker image:** downloaded separately by Docker, not included in this source ZIP. See https://hub.docker.com/_/mysql and vendor licensing.
- **python-docx:** MIT, optional report-generation tooling; https://github.com/python-openxml/python-docx/blob/master/LICENSE.
- **Java runtime, Maven, Docker, LibreOffice and fonts:** installed separately; their original licenses apply. No proprietary Tahoma font file is bundled. The editable report specifies Tahoma 10; PDF rendering may use the system fallback if Tahoma is unavailable.

Do not remove dependency notices when redistributing the executable. See Maven dependency metadata for the authoritative versions and licenses.