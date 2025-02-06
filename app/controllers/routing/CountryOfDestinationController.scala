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

import config.FrontendAppConfig
import connectors.ReferenceDataConnector.NoReferenceDataFoundException
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.SelectableFormProvider
import models.{LocalReferenceNumber, Mode}
import navigation.{RoutingNavigatorProvider, UserAnswersNavigator}
import pages.routing.CountryOfDestinationPage
import play.api.data.FormError
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.{CountriesService, CustomsOfficesService}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.routing.CountryOfDestinationView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class CountryOfDestinationController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: RoutingNavigatorProvider,
  actions: Actions,
  formProvider: SelectableFormProvider,
  countriesService: CountriesService,
  customsOfficesService: CustomsOfficesService,
  val controllerComponents: MessagesControllerComponents,
  view: CountryOfDestinationView
)(implicit ec: ExecutionContext, config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private val prefix: String = "routing.countryOfDestination"

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      countriesService.getDestinationCountries(request.userAnswers).map {
        countryList =>
          val form = formProvider(prefix, countryList)
          val preparedForm = request.userAnswers.get(CountryOfDestinationPage) match {
            case None        => form
            case Some(value) => form.fill(value)
          }

          Ok(view(preparedForm, lrn, countryList.values, mode))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      countriesService.getDestinationCountries(request.userAnswers).flatMap {
        countryList =>
          val form = formProvider(prefix, countryList)
          form
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, countryList.values, mode))),
              value =>
                customsOfficesService
                  .getCustomsOfficesOfDestinationForCountry(value.code)
                  .flatMap {
                    _ =>
                      val navigator: UserAnswersNavigator = navigatorProvider(mode)
                      CountryOfDestinationPage
                        .writeToUserAnswers(value)
                        .updateTask()
                        .writeToSession(sessionRepository)
                        .navigateWith(navigator)
                  }
                  .recover {
                    case _: NoReferenceDataFoundException =>
                      val formWithErrors = form.withError(FormError("value", s"$prefix.error.noOffices"))
                      BadRequest(view(formWithErrors, lrn, countryList.values, mode))
                  }
            )
      }
  }
}
