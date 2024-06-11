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

package controllers.routing.index

import config.PhaseConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.SelectableFormProvider
import models.reference.{Country, CustomsOffice}
import models.requests.DataRequest
import models.{Index, LocalReferenceNumber, Mode, RichOptionalJsArray, SelectableList, UserAnswers}
import navigation.{CountryOfRoutingNavigatorProvider, UserAnswersNavigator}
import pages.QuestionPage
import pages.routing.index.{CountryOfRoutingInCL112Page, CountryOfRoutingInCL147Page, CountryOfRoutingPage}
import pages.sections.Section
import pages.sections.transit.{OfficeOfTransitSection, OfficesOfTransitSection}
import pages.transit.index.OfficeOfTransitPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsArray, JsObject}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Request}
import repositories.SessionRepository
import services.CountriesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.routing.index.CountryOfRoutingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class CountryOfRoutingController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: CountryOfRoutingNavigatorProvider,
  actions: Actions,
  formProvider: SelectableFormProvider,
  countriesService: CountriesService,
  val controllerComponents: MessagesControllerComponents,
  view: CountryOfRoutingView
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(countryList: SelectableList[Country]): Form[Country] =
    formProvider("routing.index.countryOfRouting", countryList)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      val countryOfRoutingPage: Option[Country] = request.userAnswers.get(CountryOfRoutingPage(index))
      countriesService.getCountriesOfRouting(request.userAnswers, index).map {
        countryList =>
          val preparedForm = countryOfRoutingPage match {
            case None        => form(countryList)
            case Some(value) => form(countryList).fill(value)
          }
          Ok(view(preparedForm, lrn, countryList.values, mode, index))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      countriesService.getCountriesOfRouting(request.userAnswers, index).flatMap {
        countryList =>
          form(countryList)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, countryList.values, mode, index))),
              selectedCountry =>
                for {
                  isInCL112 <- countriesService.isInCL112(selectedCountry.code.code)
                  isInCL147 <- countriesService.isInCL147(selectedCountry.code.code)
                  result <- {
                    implicit val navigator: UserAnswersNavigator = navigatorProvider(mode, index)
                    CountryOfRoutingPage(index)
                      .writeToUserAnswers(selectedCountry)
                      .removeOfficesOfTransit(request.userAnswers.get(CountryOfRoutingPage(index)), selectedCountry)
                      .appendValue(CountryOfRoutingInCL112Page(index), isInCL112)
                      .appendValue(CountryOfRoutingInCL147Page(index), isInCL147)
                      .updateTask()
                      .writeToSession()
                      .navigate()

                  }
                } yield result
            )
      }
  }

}
