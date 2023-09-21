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

package controllers.locationOfGoods

import config.PhaseConfig
import controllers.actions._
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.EnumerableFormProvider
import models.LocationType.AuthorisedPlace
import models.requests.MandatoryDataRequest
import models.{LocalReferenceNumber, LocationType, Mode, ProcedureType}
import navigation.{LocationOfGoodsNavigatorProvider, UserAnswersNavigator}
import pages.external.ProcedureTypePage
import pages.locationOfGoods.LocationTypePage
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.locationOfGoods.LocationTypeView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LocationTypeController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  navigatorProvider: LocationOfGoodsNavigatorProvider,
  actions: Actions,
  formProvider: EnumerableFormProvider,
  val controllerComponents: MessagesControllerComponents,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  view: LocationTypeView
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider[LocationType]("locationOfGoods.locationType")

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(ProcedureTypePage))
    .async {
      implicit request =>
        val preparedForm = request.userAnswers.get(LocationTypePage) match {
          case None        => form
          case Some(value) => form.fill(value)
        }

        request.arg match {
          case ProcedureType.Normal     => Future.successful(Ok(view(preparedForm, lrn, LocationType.normalProcedureValues, mode)))
          case ProcedureType.Simplified => redirect(mode, AuthorisedPlace)
        }
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(ProcedureTypePage))
    .async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, LocationType.normalProcedureValues, mode))),
            value => redirect(mode, value)
          )
    }

  private def redirect(
    mode: Mode,
    value: LocationType
  )(implicit request: MandatoryDataRequest[_], hc: HeaderCarrier): Future[Result] = {
    implicit val navigator: UserAnswersNavigator = navigatorProvider(mode)
    LocationTypePage
      .writeToUserAnswers(value)
      .updateTask()
      .writeToSession()
      .navigate()
  }
}
