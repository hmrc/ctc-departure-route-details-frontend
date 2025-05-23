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

import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import controllers.actions.*
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.SelectableFormProvider.OfficeFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import navigation.{OfficeOfTransitNavigatorProvider, UserAnswersNavigator}
import pages.routing.CountryOfDestinationPage
import pages.transit.index.*
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
  sessionRepository: SessionRepository,
  navigatorProvider: OfficeOfTransitNavigatorProvider,
  actions: Actions,
  formProvider: OfficeFormProvider,
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
        customsOfficesService
          .getCustomsOfficesOfTransitForCountry(country.code)
          .map {
            customsOfficeList =>
              val form = formProvider("transit.index.officeOfTransit", customsOfficeList, country.description)
              val preparedForm = request.userAnswers.get(OfficeOfTransitPage(index)) match {
                case None        => form
                case Some(value) => form.fill(value)
              }
              Ok(view(preparedForm, lrn, customsOfficeList.values, country.description, mode, index))
          }
          .recover {
            case _: NoReferenceDataFoundException =>
              Redirect(routes.NoOfficesAvailableController.onPageLoad(lrn, index))
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
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, customsOfficeList.values, country.description, mode, index))),
                value =>
                  for {
                    isInCL147 <- countriesService.isInCL147(value.countryId)
                    isInCL010 <- countriesService.isInCL010(value.countryId)
                    result <- {
                      val navigator: UserAnswersNavigator = navigatorProvider(mode, index)
                      OfficeOfTransitPage(index)
                        .writeToUserAnswers(value)
                        .appendValue(OfficeOfTransitInCL147Page(index), isInCL147)
                        .appendValue(OfficeOfTransitInCL010Page(index), isInCL010)
                        .updateTask()
                        .writeToSession(sessionRepository)
                        .navigateWith(navigator)
                    }
                  } yield result
              )
        }
    }
}
