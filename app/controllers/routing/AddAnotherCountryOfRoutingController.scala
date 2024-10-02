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
import controllers.actions._
import controllers.routing.index.{routes => indexRoutes}
import forms.AddAnotherFormProvider
import models.{LocalReferenceNumber, Mode}
import navigation.{RoutingNavigatorProvider, UserAnswersNavigator}
import pages.sections.routing.CountriesOfRoutingSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.routing.AddAnotherCountryOfRoutingViewModel
import viewModels.routing.AddAnotherCountryOfRoutingViewModel.AddAnotherCountryOfRoutingViewModelProvider
import views.html.routing.AddAnotherCountryOfRoutingView

import javax.inject.Inject

class AddAnotherCountryOfRoutingController @Inject() (
  override val messagesApi: MessagesApi,
  navigatorProvider: RoutingNavigatorProvider,
  actions: Actions,
  formProvider: AddAnotherFormProvider,
  val controllerComponents: MessagesControllerComponents,
  viewModelProvider: AddAnotherCountryOfRoutingViewModelProvider,
  view: AddAnotherCountryOfRoutingView
)(implicit config: FrontendAppConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: AddAnotherCountryOfRoutingViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.allowMore)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, mode)
      viewModel.count match {
        case 0 => Redirect(routes.BindingItineraryController.onPageLoad(lrn, mode))
        case _ => Ok(view(form(viewModel), lrn, viewModel))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn) {
    implicit request =>
      val viewModel = viewModelProvider(request.userAnswers, mode)
      form(viewModel)
        .bindFromRequest()
        .fold(
          formWithErrors => BadRequest(view(formWithErrors, lrn, viewModel)),
          {
            case true =>
              Redirect(indexRoutes.CountryOfRoutingController.onPageLoad(lrn, mode, viewModel.nextIndex))
            case false =>
              val navigator: UserAnswersNavigator = navigatorProvider(mode)
              Redirect(navigator.nextPage(request.userAnswers, Some(CountriesOfRoutingSection)))
          }
        )
  }
}
