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

import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.CountryFormProvider
import models.reference.Country
import models.{CountryList, Index, LocalReferenceNumber, Mode}
import navigation.{CountryOfRoutingNavigatorProvider, UserAnswersNavigator}
import pages.routing.index.CountryOfRoutingPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.CountriesService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.routing.index.CountryOfRoutingView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CountryOfRoutingController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: CountryOfRoutingNavigatorProvider,
  actions: Actions,
  formProvider: CountryFormProvider,
  countriesService: CountriesService,
  val controllerComponents: MessagesControllerComponents,
  view: CountryOfRoutingView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(countryList: CountryList): Form[Country] =
    formProvider("routing.index.countryOfRouting", countryList)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      countriesService.getCountries().map {
        countryList =>
          val preparedForm = request.userAnswers.get(CountryOfRoutingPage(index)) match {
            case None        => form(countryList)
            case Some(value) => form(countryList).fill(value)
          }

          Ok(view(preparedForm, lrn, countryList.countries, mode, index))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      countriesService.getCountries().flatMap {
        countryList =>
          form(countryList)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, countryList.countries, mode, index))),
              value =>
                for {
                  ctcCountries                          <- countriesService.getCountryCodesCTC()
                  customsSecurityAgreementAreaCountries <- countriesService.getCustomsSecurityAgreementAreaCountries()
                  result <- {
                    implicit val navigator: UserAnswersNavigator = navigatorProvider(mode, index, ctcCountries, customsSecurityAgreementAreaCountries)
                    CountryOfRoutingPage(index)
                      .writeToUserAnswers(value)
                      .updateTask(ctcCountries, customsSecurityAgreementAreaCountries)
                      .writeToSession()
                      .navigate()
                  }
                } yield result
            )
      }
  }
}
