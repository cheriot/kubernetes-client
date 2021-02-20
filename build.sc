import $ivy.`com.goyeau::mill-git:0.1.0-6-4254b37`
import $ivy.`com.goyeau::mill-scalafix:8515ae6`
import $ivy.`com.lihaoyi::mill-contrib-bsp:$MILL_VERSION`
import $ivy.`io.github.davidgregory084::mill-tpolecat:0.1.3`
import $file.project.Dependencies, Dependencies.Dependencies._
import $file.project.{SwaggerModelGenerator => SwaggerModelGeneratorFile}
import SwaggerModelGeneratorFile.SwaggerModelGenerator
import com.goyeau.mill.git.GitVersionedPublishModule
import com.goyeau.mill.scalafix.ScalafixModule
import io.github.davidgregory084.TpolecatModule
import mill._
import mill.scalalib._
import mill.scalajslib._
import mill.scalalib.publish.{Developer, License, PomSettings, VersionControl}
import mill.scalalib.scalafmt.ScalafmtModule

val scalaVersions = "2.13.2" :: "2.12.11" :: Nil

object `kubernetes-client` extends Module {

  object jvm extends Cross[JVMKubernetesClientModule](scalaVersions: _*)
  object js extends Cross[JSKubernetesClientModule](scalaVersions: _*)
}

class JVMKubernetesClientModule(val crossScalaVersion: String)
    extends KubernetesClientModule {
}

class JSKubernetesClientModule(val crossScalaVersion: String)
    extends KubernetesClientModule
    with ScalaJSModule {

  override def scalaJSVersion = "1.5.0"
}

trait KubernetesClientModule
    extends CrossScalaModule
    with TpolecatModule
    with ScalafmtModule
    with ScalafixModule
    with GitVersionedPublishModule
    with SwaggerModelGenerator {

  override def scalacOptions =
    super.scalacOptions().filter(_ != "-Wunused:imports") ++
      (if (crossScalaVersion.startsWith("2.12")) Seq("-Ypartial-unification") else Seq.empty)
  override def ivyDeps =
    super.ivyDeps() ++ http4s ++ akkaHttp ++ circe ++ circeYaml ++ bouncycastle ++ collectionCompat ++ logging

  object test extends Tests {
    def testFrameworks    = Seq("org.scalatest.tools.Framework")
    override def forkArgs = super.forkArgs() :+ "-Djdk.tls.client.protocols=TLSv1.2"
    override def ivyDeps  = super.ivyDeps() ++ Agg(ivy"org.scalatest::scalatest:3.1.1")
  }

  def millSourcePath = super.millSourcePath / os.up
  override def artifactName = "kubernetes-client"
  def pomSettings =
    PomSettings(
      description = "A Kubernetes client for Scala",
      organization = "com.goyeau",
      url = "https://github.com/joan38/kubernetes-client",
      licenses = Seq(License.`Apache-2.0`),
      versionControl = VersionControl.github("joan38", "kubernetes-client"),
      developers = Seq(Developer("joan38", "Joan Goyeau", "https://github.com/joan38"))
    )
}
