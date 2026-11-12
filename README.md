# Scala Next + sbt

A small Scala 3 project with strict compiler checks, formatting, unit and property tests,
dependency integrity checks, and GitHub Actions. Use it as the starting point for a Scala application
or library, then replace the sample `example.Greeter` with your own code.

Scala Next means the current stable feature release of Scala 3, rather than an LTS,
release candidate, or nightly. This template uses **Scala 3.10.1** and **sbt 2.0.12**.
## Start

Install a maintained **JDK 21 or JDK 25**, Bash, curl, and either `sha256sum` or `shasum`.
Linux and macOS can use the wrapper directly; on Windows, use WSL. A native Windows wrapper is not included.

```sh
./sbtw check
./sbtw audit
```

The first run downloads the exact sbt launcher and checks it against the committed SHA-256.
Every subsequent run checks that digest again. Java options are read from `.jvmopts` as
one argument per line, without shell evaluation. `JAVA_HOME` selects the JDK when set.
The wrapper isolates user-installed sbt global plugins and restricts resolution to Maven Central.

Open the folder in VS Code with Metals, or import `build.sbt` in IntelliJ IDEA.
Editors can use their own sbt installation; `./sbtw` is the supported command for CI and local checks.

## Daily commands

| Command | Purpose |
| --- | --- |
| `./sbtw compile` | Compile with strict warnings |
| `./sbtw 'testOnly *'` | Run all unit and ScalaCheck property tests |
| `./sbtw 'testOnly example.GreeterSuite'` | Run one suite |
| `./sbtw fmt` | Format Scala sources and build definitions |
| `./sbtw check` | Check dependency integrity and formatting; clean, test, and package |
| `./sbtw audit` | Scan resolved dependencies, including test libraries |
| `./sbtw dependencyTree` | Inspect the resolved dependency graph |
| `./sbtw writeDependencyManifest` | Refresh the reviewed version and JAR-hash baseline |

sbt 2 can use incremental `testQuick` behavior for `test`. The `check` alias uses
`testOnly *` so every suite runs during validation. It also rejects a missing or changed dependency manifest.

The sample returns `Either[String, String]` for validation, with tests for empty names,
whitespace, Unicode, and general properties. There is no application entry point yet.
`package` produces a library JAR; add your application's entry point when needed.

## Compiler and formatting policy

Both main and test code use `-Werror`, `-Wunused:all`, `-Wvalue-discard`,
`-Wnonunit-statement`, and `-Wsafe-init`, alongside deprecation, feature, and unchecked warnings.
Java uses `-Xlint:all -Werror`. Both target Java 21 bytecode, even when built on JDK 25.
Incompatible dependency evictions fail the build.

Scalafmt checks the main sources, tests, and sbt definitions. Compiler warnings provide
semantic linting without a compiler plugin tied to a specific Scala patch release.
Scalafix and WartRemover are not installed; add them only after confirming Scala Next
and sbt 2 compatibility. Experimental language features are opt-in.

## Dependencies and security

Direct dependencies and build plugins use exact versions. The committed
`project/dependencies.sha256` records every resolved application and test JAR's Maven
coordinates and SHA-256. `verifyDependencies` rehashes the resolved JARs without task caching;
`check` fails if versions or bytes differ, or if the manifest is missing. Resolution is
still performed by sbt: this manifest detects drift rather than forcing a particular
transitive resolution. Review the diff before regenerating it.

Native sbt 2 dependency locking is not enabled because it conflicted with the selected
formatter's dynamic resolution during validation. Do not add a root `deps.lock` without
rechecking that integration. The launcher has a separate SHA-256 pin in
`project/sbt-launcher.sha256`.

The OSV audit scans direct and transitive Maven dependencies in compile, runtime, test,
provided, and optional scopes. It fails on any reported vulnerability, including a zero or
missing CVSS score, and on an API/network error. It uses fresh responses, finite timeouts,
and no preconfigured suppressions. HTML and JSON reports are stored below
`target/**/security/`. Package-supplied suppressions are disabled.

The audit covers the application's resolved dependency graph, including tests. It does
not scan every dependency inside sbt, Scalafmt's dynamically loaded engine, or Actions.
Build plugins use exact declared versions; Renovate monitors declared build tools and Actions.
Review scanner reports and upstream security advisories when upgrading those tools.
The scanner sends public Maven coordinates and versions to `api.osv.dev`.

CI runs on pull requests, merges to `main`, and merge queues. It checks JDK 21 and 25.
Dependency auditing also runs every Monday at 07:23 UTC. Actions are pinned to full commit
SHAs, tokens have read-only permissions, checkout does not persist credentials, and jobs
use hosted runners with timeouts. Pull request jobs receive no repository secrets.
No workflow uses `pull_request_target`, publishes packages, or executes pull request code
with write permissions. Shared dependency caches are intentionally omitted.

## Updates

Install the [Renovate GitHub App](https://github.com/apps/renovate) for your new repository.
The config monitors sbt, Scalafmt, and Actions, waits seven days for ordinary releases,
and requires review before merging. Keep security fixes moving promptly even if they
need manual updates outside that schedule.

After changing a dependency or Scala version:

```sh
./sbtw writeDependencyManifest
./sbtw check audit
```

Review the manifest diff. Renovate does not regenerate this integrity baseline here;
a maintainer must update it before merging. A stale manifest deliberately fails CI.
When moving to a new Scala minor, also review the `-source:3.10` setting.

After an sbt update, review the upstream release and artifact, calculate the new launcher's
SHA-256, and update the version/digest pair in `project/sbt-launcher.sha256` together with
`project/build.properties`. The wrapper refuses a version without a matching pin.
Do not replace a checksum merely to bypass a mismatch; investigate the artifact first.

## Use this template

For each generated project:

1. Rename the project and organization in `build.sbt`, and replace the `example` package.
2. Update `.github/CODEOWNERS` and the license attribution for your project.
3. Install Renovate and enable Actions, Dependabot alerts, and secret scanning where available.
4. Require `Build (Java 21)`, `Build (Java 25)`, and `Audit dependencies` on your default branch.
   Require pull request review and code-owner review; restrict force pushes and branch deletion.
5. Review [SECURITY.md](SECURITY.md) and enable private vulnerability reporting.

Branch rules, app installations, secret scanning, and private reporting are repository settings;
copying template files does not enable them. Publishing is disabled in sbt until you add
project-specific release metadata and credentials. The template is MIT-licensed.
