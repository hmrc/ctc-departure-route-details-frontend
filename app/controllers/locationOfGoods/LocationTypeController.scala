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
import models.requests.MandatoryDataRequest
import models.{LocalReferenceNumber, LocationType, Mode, NormalMode, ProcedureType}
import navigation.{LocationOfGoodsNavigatorProvider, UserAnswersNavigator}
import pages.external.ProcedureTypePage
import pages.locationOfGoods.LocationTypePage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import repositories.SessionRepository
import services.LocationTypeService
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
  locationTypeService: LocationTypeService,
  val controllerComponents: MessagesControllerComponents,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  view: LocationTypeView
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
    extends FrontendBaseController
    with I18nSupport {

  private def form(locationType: Seq[LocationType]): Form[LocationType] =
    formProvider("locationOfGoods.locationType", locationType)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(ProcedureTypePage))
    .async {
      implicit request =>
        locationTypeService.getLocationTypes().map {
          locationTypes =>
            val preparedForm = request.userAnswers.get(LocationTypePage) match {
              case None        => form(locationTypes)
              case Some(value) => form(locationTypes).fill(value)
            }

            request.arg match {
              case ProcedureType.Normal => Future.successful(Ok(view(preparedForm, lrn, locationTypes, mode)))
              case ProcedureType.Simplified =>
                Future.successful(Redirect(controllers.locationOfGoods.routes.AuthorisationNumberController.onPageLoad(lrn, NormalMode)))
            }
            Ok(view(preparedForm, lrn, locationTypes, mode))
        }
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode): Action[AnyContent] = actions
    .requireData(lrn)
    .andThen(getMandatoryPage(ProcedureTypePage))
    .async {
      implicit request =>
        locationTypeService.getLocationTypes().flatMap {
          locationTypes =>
            form(locationTypes)
              .bindFromRequest()
              .fold(
                formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, locationTypes, mode))),
                value => redirect(mode, value)
              )

        }
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
