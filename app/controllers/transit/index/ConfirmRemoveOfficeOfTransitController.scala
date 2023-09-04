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

import config.PhaseConfig
import controllers.actions._
import controllers.transit.{routes => transitRoutes}
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.YesNoFormProvider
import models.{Index, LocalReferenceNumber, Mode}
import pages.sections.transit.OfficeOfTransitSection
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewModels.transit.RemoveOfficeOfTransitViewModel
import viewModels.transit.RemoveOfficeOfTransitViewModel.RemoveOfficeOfTransitViewModelProvider
import views.html.transit.index.ConfirmRemoveOfficeOfTransitView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ConfirmRemoveOfficeOfTransitController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: ConfirmRemoveOfficeOfTransitView,
  viewModelProvider: RemoveOfficeOfTransitViewModelProvider
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(viewModel: RemoveOfficeOfTransitViewModel): Form[Boolean] =
    formProvider(viewModel.prefix, viewModel.args: _*)

  private def addAnother(lrn: LocalReferenceNumber, mode: Mode): Call =
    transitRoutes.AddAnotherOfficeOfTransitController.onPageLoad(lrn, mode)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions
    .requireIndex(lrn, OfficeOfTransitSection(index), addAnother(lrn, mode)) {
      implicit request =>
        val viewModel = viewModelProvider.apply(request.userAnswers, index)
        Ok(view(form(viewModel), lrn, mode, index, viewModel))
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions
    .requireIndex(lrn, OfficeOfTransitSection(index), addAnother(lrn, mode))
    .async {
      implicit request =>
        val viewModel = viewModelProvider.apply(request.userAnswers, index)
        form(viewModel)
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, index, viewModel))),
            {
              case true =>
                OfficeOfTransitSection(index)
                  .removeFromUserAnswers()
                  .updateTask()
                  .writeToSession()
                  .navigateTo(addAnother(lrn, mode))
              case false =>
                Future.successful(Redirect(addAnother(lrn, mode)))
            }
          )
    }
}
