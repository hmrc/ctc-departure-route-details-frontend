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

import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.CustomsOfficeForCountryFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{OfficeOfTransitNavigatorProvider, UserAnswersNavigator}
import pages.routing.CountryOfDestinationPage
import pages.transit.index.{InferredOfficeOfTransitCountryPage, OfficeOfTransitCountryPage, OfficeOfTransitInCL147Page, OfficeOfTransitPage}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{CountriesService, CustomsOfficesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.transit.index.OfficeOfTransitView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class OfficeOfTransitController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: OfficeOfTransitNavigatorProvider,
  actions: Actions,
  formProvider: CustomsOfficeForCountryFormProvider,
  customsOfficesService: CustomsOfficesService,
  countriesService: CountriesService,
  val controllerComponents: MessagesControllerComponents,
  view: OfficeOfTransitView,
  getMandatoryPage: SpecificDataRequiredActionProvider
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(OfficeOfTransitCountryPage(index), InferredOfficeOfTransitCountryPage(index), CountryOfDestinationPage))
    .async {
      implicit request =>
        val country = request.arg
        customsOfficesService.getCustomsOfficesOfTransitForCountry(country.code).map {
          customsOfficeList =>
            val form = formProvider("transit.index.officeOfTransit", customsOfficeList, country.description)
            val preparedForm = request.userAnswers.get(OfficeOfTransitPage(index)) match {
              case None        => form
              case Some(value) => form.fill(value)
            }
            Ok(view(preparedForm, lrn, customsOfficeList.customsOffices, country.description, mode, index))
        }
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(OfficeOfTransitCountryPage(index), InferredOfficeOfTransitCountryPage(index), CountryOfDestinationPage))
    .async {
      implicit request =>
        val country = request.arg
        customsOfficesService.getCustomsOfficesOfTransitForCountry(country.code).flatMap {
          customsOfficeList =>
            val form = formProvider("transit.index.officeOfTransit", customsOfficeList, country.description)
            form
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, customsOfficeList.customsOffices, country.description, mode, index))),
                value =>
                  for {
                    customsSecurityAgreementAreaCountries <- countriesService.getCustomsSecurityAgreementAreaCountries().map(_.values)
                    isInCL147 = customsSecurityAgreementAreaCountries.map(_.code.code).contains(value.countryCode)
                    result <- {
                      implicit val navigator: UserAnswersNavigator = navigatorProvider(mode, index)
                      OfficeOfTransitPage(index)
                        .writeToUserAnswers(value)
                        .appendValue(OfficeOfTransitInCL147Page(index), isInCL147)
                        .updateTask()
                        .writeToSession()
                        .navigate()
                    }
                  } yield result
              )
        }
    }
}
