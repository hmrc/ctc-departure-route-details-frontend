/*
 * Copyright 2023 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package controllers.exit.index

import config.FrontendAppConfig
import controllers.actions._
import controllers.exit.{routes => exitRoutes}
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.YesNoFormProvider
import models.reference.CustomsOffice
import models.requests.DataRequest
import models.{Index, LocalReferenceNumber, Mode}
import pages.exit.index.OfficeOfExitPage
import pages.sections.exit.OfficeOfExitSection
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.CountriesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.exit.index.ConfirmRemoveOfficeOfExitView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmRemoveOfficeOfExitController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ConfirmRemoveOfficeOfExitView,
  countriesService: CountriesService
)(implicit ec: ExecutionContext, config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private case class DynamicHeading(prefix: String, args: String*)

  private def dynamicHeading(index: Index)(implicit request: DataRequest[_]): Option[DynamicHeading] =
    request.userAnswers.get(OfficeOfExitSection(index)) map {
      _ =>
        val prefix = "exit.index.confirmRemoveOfficeOfExit"
        request.userAnswers.get(OfficeOfExitPage(index)) match {
          case Some(CustomsOffice(_, name, _)) => DynamicHeading(prefix, name)
          case None                            => DynamicHeading(s"$prefix.default")
        }
    }

  def onPageLoad(lrn: LocalReferenceNumber, index: Index, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      dynamicHeading(index) match {
        case Some(DynamicHeading(prefix, args @ _*)) =>
          Ok(view(formProvider(prefix, args: _*), lrn, index, mode, prefix, args: _*))
        case _ => Redirect(config.sessionExpiredUrl)
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, index: Index, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      dynamicHeading(index) match {
        case Some(DynamicHeading(prefix, args @ _*)) =>
          formProvider(prefix, args: _*)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, index, mode, prefix, args: _*))),
              {
                case true =>
                  for {
                    ctcCountries                          <- countriesService.getCountryCodesCTC()
                    customsSecurityAgreementAreaCountries <- countriesService.getCustomsSecurityAgreementAreaCountries()
                    result <- OfficeOfExitSection(index)
                      .removeFromUserAnswers()
                      .updateTask(ctcCountries, customsSecurityAgreementAreaCountries)
                      .writeToSession()
                      .navigateTo(exitRoutes.AddAnotherOfficeOfExitController.onPageLoad(lrn, mode))
                  } yield result
                case false =>
                  Future.successful(Redirect(exitRoutes.AddAnotherOfficeOfExitController.onPageLoad(lrn, mode)))
              }
            )
        case _ => Future.successful(Redirect(config.sessionExpiredUrl))
      }
  }
}
