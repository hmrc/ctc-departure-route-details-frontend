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

import config.PhaseConfig
import controllers.actions._
import controllers.routing.{routes => routingRoutes}
import controllers.{NavigatorOps, SettableOps, SettableOpsRunner}
import forms.YesNoFormProvider
import models.reference.{Country, CustomsOffice}
import models.requests.SpecificDataRequestProvider1
import models.{Index, LocalReferenceNumber, Mode, UserAnswers}
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
import play.api.mvc.{Action, AnyContent, Call, MessagesControllerComponents, Result}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.routing.index.RemoveCountryOfRoutingYesNoView

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class RemoveCountryOfRoutingYesNoController @Inject() (
  override val messagesApi: MessagesApi,
  implicit val sessionRepository: SessionRepository,
  actions: Actions,
  getMandatoryPage: SpecificDataRequiredActionProvider,
  formProvider: YesNoFormProvider,
  val controllerComponents: MessagesControllerComponents,
  view: RemoveCountryOfRoutingYesNoView
)(implicit ec: ExecutionContext, phaseConfig: PhaseConfig)
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
                      .flatMap(findAndRemoveOffices(request, _, OfficesOfTransitSection, OfficeOfTransitPage, OfficeOfTransitSection))
                      .flatMap(findAndRemoveOffices(request, _, OfficesOfExitSection, OfficeOfExitPage, OfficeOfExitSection))
                  )
                  _ <- sessionRepository.set(updatedAnswers)
                } yield Redirect(addAnother(lrn, mode))

              case false =>
                Future.successful(Redirect(addAnother(lrn, mode)))
            }
          )
    }

  private def findAndRemoveOffices(request: SpecificDataRequestProvider1[Country]#SpecificDataRequest[AnyContent],
                                   userAnswers: UserAnswers,
                                   sections: Section[JsArray],
                                   page: Index => QuestionPage[CustomsOffice],
                                   section: Index => Section[JsObject]
  ): Try[UserAnswers] = {

    case class OfficeWithIndex(office: CustomsOffice, index: Index)

    val officesWithIndex: Seq[OfficeWithIndex] = request.userAnswers
      .get(sections)
      .map(_.value.zipWithIndex.flatMap {
        case (_, index) => request.userAnswers.get(page(Index(index))).map(OfficeWithIndex(_, Index(index)))
      })
      .getOrElse(Seq.empty[OfficeWithIndex])
      .toSeq

    val officesToDelete: Seq[Index] = officesWithIndex.collect {
      case officeWithIndex if officeWithIndex.office.countryId == request.arg.code.code => officeWithIndex.index
    }.reverse

    officesToDelete.foldLeft(Try(userAnswers)) {
      case (acc, index) =>
        acc.flatMap {
          _.remove(section(index))
        }
    }
  }
}
