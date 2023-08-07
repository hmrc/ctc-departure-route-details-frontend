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

package controllers.routing

import config.PhaseConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.SelectableFormProvider
import models.reference.CustomsOffice
import models.{LocalReferenceNumber, Mode, SelectableList}
import navigation.{RoutingNavigatorProvider, UserAnswersNavigator}
import pages.routing.{CountryOfDestinationPage, OfficeOfDestinationInCL112Page, OfficeOfDestinationPage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{CountriesService, CustomsOfficesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.routing.OfficeOfDestinationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OfficeOfDestinationController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: RoutingNavigatorProvider,
  actions: Actions,
  formProvider: SelectableFormProvider,
  customsOfficesService: CustomsOfficesService,
  countriesService: CountriesService,
  val controllerComponents: MessagesControllerComponents,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  view: OfficeOfDestinationView
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(customsOfficeList: SelectableList[CustomsOffice]): Form[CustomsOffice] =
    formProvider("routing.officeOfDestination", customsOfficeList)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(CountryOfDestinationPage))
    .async {
      implicit request =>
        val country = request.arg
        customsOfficesService.getCustomsOfficesOfDestinationForCountry(country.code).map {
          customsOfficeList =>
            val preparedForm = request.userAnswers.get(OfficeOfDestinationPage) match {
              case None        => form(customsOfficeList)
              case Some(value) => form(customsOfficeList).fill(value)
            }

            Ok(view(preparedForm, lrn, country.description, customsOfficeList.values, mode))
        }
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(CountryOfDestinationPage))
    .async {
      implicit request =>
        val country = request.arg
        customsOfficesService.getCustomsOfficesOfDestinationForCountry(country.code).flatMap {
          customsOfficeList =>
            form(customsOfficeList)
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, country.description, customsOfficeList.values, mode))),
                value =>
                  for {
                    ctcCountries <- countriesService.getCountryCodesCTC().map(_.values)
                    isInCL112 = ctcCountries.map(_.code.code).contains(value.countryCode)
                    result <- {
                      implicit val navigator: UserAnswersNavigator = navigatorProvider(mode)
                      OfficeOfDestinationPage
                        .writeToUserAnswers(value)
                        .appendValue(OfficeOfDestinationInCL112Page, isInCL112)
                        .updateTask()
                        .writeToSession()
                        .navigate()
                    }
                  } yield result
              )
        }
    }
}
