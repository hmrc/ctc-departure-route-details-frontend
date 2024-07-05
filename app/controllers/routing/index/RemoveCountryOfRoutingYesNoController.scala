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
import controllers.routing.{routes => routingRoutes}
import forms.YesNoFormProvider
import models.reference.{Country, CustomsOffice}
import models.requests.SpecificDataRequestProvider1
import models.{Index, LocalReferenceNumber, Mode, RichOptionalJsArray, UserAnswers}
import pages.QuestionPage
import pages.exit.index.OfficeOfExitPage
import pages.routing.index.CountryOfRoutingPage
import pages.sections.Section
import pages.sections.exit.{OfficeOfExitSection, OfficesOfExitSection}
import pages.sections.routing.CountryOfRoutingSection
import pages.sections.transit.{OfficeOfTransitSection, OfficesOfTransitSection}
import pages.transit.index.OfficeOfTransitPage
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.{JsArray, JsObject}
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.routing.index.RemoveCountryOfRoutingYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class RemoveCountryOfRoutingYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  sessionRepository: SessionRepository,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveCountryOfRoutingYesNoView
)(implicit ec: ExecutionContext)
    extends FrontendBaseController
    with I18nSupport {

  private def addAnother(lrn: LocalReferenceNumber, mode: Mode): Call =
    routingRoutes.AddAnotherCountryOfRoutingController.onPageLoad(lrn, mode)

  private type Request = SpecificDataRequestProvider1[Country]#SpecificDataRequest[_]

  private def form(implicit request: Request): Form[Boolean] =
    formProvider("routing.index.removeCountryOfRoutingYesNo", request.arg.toString)

  def onPageLoad(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions
    .requireIndex(lrn, CountryOfRoutingSection(index), addAnother(lrn, mode))
    .andThen(getMandatoryPage(CountryOfRoutingPage(index))) {
      implicit request =>
        Ok(view(form, lrn, mode, index, request.arg))
    }

  def onSubmit(lrn: LocalReferenceNumber, mode: Mode, index: Index): Action[AnyContent] = actions
    .requireIndex(lrn, CountryOfRoutingSection(index), addAnother(lrn, mode))
    .andThen(getMandatoryPage(CountryOfRoutingPage(index)))
    .async {
      implicit request =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => Future.successful(BadRequest(view(formWithErrors, lrn, mode, index, request.arg))),
            {
              case true =>
                for {
                  updatedAnswers <- Future.fromTry(
                    request.userAnswers
                      .remove(CountryOfRoutingSection(index))
                      .flatMap(findAndRemoveOffices(_, OfficesOfTransitSection, OfficeOfTransitSection, OfficeOfTransitPage))
                      .flatMap(findAndRemoveOffices(_, OfficesOfExitSection, OfficeOfExitSection, OfficeOfExitPage))
                  )
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(addAnother(lrn, mode))

              case false =>
                Future.successful(Redirect(addAnother(lrn, mode)))
            }
          )
    }

  private def findAndRemoveOffices(
    userAnswers: UserAnswers,
    array: Section[JsArray],
    obj: Index => Section[JsObject],
    page: Index => QuestionPage[CustomsOffice]
  )(implicit request: Request): Try[UserAnswers] =
    (0 until userAnswers.get(array).length).foldRight(Try(userAnswers)) {
      case (index, acc) =>
        userAnswers.get(page(Index(index))) match {
          case Some(value) if value.countryId == request.arg.code.code => acc.flatMap(_.remove(obj(Index(index))))
          case _                                                       => acc
        }
    }
}
