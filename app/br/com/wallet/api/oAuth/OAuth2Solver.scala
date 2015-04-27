package br.com.wallet.api.oAuth

import play.api.Application
import play.api.Play
import play.api.http.{MimeTypes, HeaderNames}
import play.api.libs.ws.WS
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import play.api.mvc.{AnyContent, Results, Action}
import play.api.mvc.Results._

/**
 * Created by sirkleber on 26/04/15.
 */
object OAuth2Solver {
  lazy val app: Application = Play.current
  private def authKey: Option[String] = app.configuration.getString("github.client.id")
  private def authSec: Option[String] = app.configuration.getString("github.client.secret")

  def getAuthorizationUrl: String => String => String => Option[String] = redirectUri => scope => state => for {
    baseUrl <- app.configuration.getString("github.redirect.url")
    authId <- app.configuration.getString("github.client.id")
  } yield baseUrl.format(authId, scope, state)

  private def getToken: String => Future[Option[String]] = code => (for {
    authSec <- app.configuration.getString("github.client.secret")
    authId <- app.configuration.getString("github.client.id")
  } yield {
      def tokenResponse = WS.url("https://github.com/login/oauth/access_token")(app).
        withQueryString("client_id" -> authId, "client_secret" -> authSec, "code" -> code).
        withHeaders(HeaderNames.ACCEPT -> MimeTypes.JSON).
        post(Results.EmptyContent())

      for {
        response <- tokenResponse
      } yield (response.json \ "access_token").asOpt[String]
    }).getOrElse(Future(None))

  def callbackUrl: Option[String] => Option[String] => Action[AnyContent] = codeOpt => stateOpt => Action.async { implicit request =>
    (for {
      code <- codeOpt
      state <- stateOpt
      oauthState <- request.session.get("oauth-state")
    } yield {
        if(oauthState == state){
          for {
            accessToken <- getToken(code)
            } yield Redirect("")
        } else {
          Future.successful(BadRequest(""))
        }
      }).getOrElse(Future.successful(BadRequest("")))
  }
}
