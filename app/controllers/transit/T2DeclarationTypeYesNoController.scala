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

package controllers.transit

import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import controllers.actions._
import forms.YesNoFormProvider
import models.{LocalReferenceNumber, Mode}
import navigation.{TransitNavigatorProvider, UserAnswersNavigator}
import pages.transit.T2DeclarationTypeYesNoPage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.CountriesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transit.T2DeclarationTypeYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class T2DeclarationTypeYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: TransitNavigatorProvider,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: T2DeclarationTypeYesNoView,
  countriesService: CountriesService
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider("transit.t2DeclarationTypeYesNo")

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val preparedForm = request.userAnswers.get(T2DeclarationTypeYesNoPage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }

      Ok(view(preparedForm, lrn, mode))
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode))),
          value =>
            for {
              ctcCountries                          <- countriesService.getCountryCodesCTC()
              customsSecurityAgreementAreaCountries <- countriesService.getCustomsSecurityAgreementAreaCountries()
              result <- {
                implicit val navigator: UserAnswersNavigator = navigatorProvider(mode, ctcCountries, customsSecurityAgreementAreaCountries)
                T2DeclarationTypeYesNoPage
                  .writeToUserAnswers(value)
                  .updateTask(ctcCountries, customsSecurityAgreementAreaCountries)
                  .writeToSession()
                  .navigate()
              }
            } yield result
        )
  }
}
