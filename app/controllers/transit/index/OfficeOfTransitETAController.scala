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

package controllers.transit.index

import config.FrontendAppConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.DateTimeFormProvider
import models.{DateTime, Index, LocalReferenceNumber, Mode}
import navigation.{OfficeOfTransitNavigatorProvider, UserAnswersNavigator}
import pages.routing.CountryOfDestinationPage
import pages.transit.index.{OfficeOfTransitCountryPage, OfficeOfTransitETAPage, OfficeOfTransitPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.DateTimeService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transit.index.OfficeOfTransitETAView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OfficeOfTransitETAController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: OfficeOfTransitNavigatorProvider,
  formProvider: DateTimeFormProvider,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  val controllerComponents: MessagesControllerComponents,
  view: OfficeOfTransitETAView,
  dateTimeService: DateTimeService
)(implicit ec: ExecutionContext, config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form: Form[DateTime] = {
    val today      = dateTimeService.today
    val pastDate   = today.minusDays(config.etaDateDaysBefore)
    val futureDate = today.plusDays(config.etaDateDaysAfter)
    formProvider("transit.index.officeOfTransitETA", pastDate, futureDate)
  }

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(OfficeOfTransitCountryPage(index), CountryOfDestinationPage))
    .andThen(getMandatoryPage.getSecond(OfficeOfTransitPage(index))) {
      implicit request =>
        request.arg match {
          case (country, customsOffice) =>
            val preparedForm = request.userAnswers.get(OfficeOfTransitETAPage(index)) match {
              case None        => form
              case Some(value) => form.fill(value)
            }
            Ok(view(preparedForm, lrn, country.description, customsOffice.name, mode, index))
        }
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(OfficeOfTransitCountryPage(index), CountryOfDestinationPage))
    .andThen(getMandatoryPage.getSecond(OfficeOfTransitPage(index)))
    .async {
      implicit request =>
        request.arg match {
          case (country, customsOffice) =>
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, country.description, customsOffice.name, mode, index))),
                value => {
                  val navigator: UserAnswersNavigator = navigatorProvider(mode, index)
                  OfficeOfTransitETAPage(index)
                    .writeToUserAnswers(value)
                    .updateTask()
                    .writeToSession(sessionRepository)
                    .navigateWith(navigator)
                }
              )
        }
    }
}
