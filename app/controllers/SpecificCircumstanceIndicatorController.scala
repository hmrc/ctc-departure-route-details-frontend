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

package controllers

import controllers.actions.*
import forms.EnumerableFormProvider
import models.reference.SpecificCircumstanceIndicator
import models.{LocalReferenceNumber, Mode}
import navigation.{RouteDetailsNavigatorProvider, UserAnswersNavigator}
import pages.SpecificCircumstanceIndicatorPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import services.SpecificCircumstanceIndicatorsService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.SpecificCircumstanceIndicatorView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class SpecificCircumstanceIndicatorController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  navigatorProvider: RouteDetailsNavigatorProvider,
  actions: Actions,
  formProvider: EnumerableFormProvider,
  specificCircumstanceIndicatorsService: SpecificCircumstanceIndicatorsService,
  val controllerComponents: MessagesControllerComponents,
  view: SpecificCircumstanceIndicatorView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def form(specificCircumstanceIndicators: Seq[SpecificCircumstanceIndicator]): Form[SpecificCircumstanceIndicator] =
    formProvider("specificCircumstanceIndicator", specificCircumstanceIndicators)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      specificCircumstanceIndicatorsService.getSpecificCircumstanceIndicators().map {
        specificCircumstanceIndicators =>
          val preparedForm = request.userAnswers.get(SpecificCircumstanceIndicatorPage) match {
            case None        => form(specificCircumstanceIndicators)
            case Some(value) => form(specificCircumstanceIndicators).fill(value)
          }

          Ok(view(preparedForm, lrn, specificCircumstanceIndicators, mode))
      }
  }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions.requireData(lrn).async {
    implicit request =>
      specificCircumstanceIndicatorsService.getSpecificCircumstanceIndicators().flatMap {
        specificCircumstanceIndicators =>
          form(specificCircumstanceIndicators)
            .bindFromRequest()
            .fold(
              formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, specificCircumstanceIndicators, mode))),
              value => {
                val navigator: UserAnswersNavigator = navigatorProvider(mode)
                SpecificCircumstanceIndicatorPage
                  .writeToUserAnswers(value)
                  .updateTask()
                  .writeToSession(sessionRepository)
                  .navigateWith(navigator)
              }
            )
      }
  }
}
