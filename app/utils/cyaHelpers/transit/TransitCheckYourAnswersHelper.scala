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

package utils.cyaHelpers.transit

import models.{Index, Mode, UserAnswers}
import play.api.i18n.Messages
import play.api.mvc.Call
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import utils.cyaHelpers.AnswersHelper
import viewModels.{Link, ListItem}

class TransitCheckYourAnswersHelper(
  userAnswers: UserAnswers,
  mode: Mode
)(
  ctcCountryCodes: Seq[String],
  customsSecurityAgreementAreaCountryCodes: Seq[String]
)(implicit messages: Messages)
    extends AnswersHelper(userAnswers, mode) {

  def includesT2Declarations: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = T2DeclarationTypeYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "routeDetails.transit.t2DeclarationTypeYesNo",
    id = Some("change-includes-t2-declarations")
  )

  def addOfficeOfTransit: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddOfficeOfTransitYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "routeDetails.transit.addOfficeOfTransitYesNo",
    id = Some("change-add-office-of-transit")
  )

  def officesOfTransit: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(OfficesOfTransitSection)(officeOfTransit)

  def officeOfTransit(index: Index): Option[SummaryListRow] = getAnswerAndBuildSectionRow[OfficeOfTransitDomain](
    formatAnswer = _.label.toText,
    prefix = "routeDetails.checkYourAnswers.transit.officeOfTransit",
    id = Some(s"change-office-of-transit-${index.display}"),
    args = index.display
  )(OfficeOfTransitDomain.userAnswersReader(index, ctcCountryCodes, customsSecurityAgreementAreaCountryCodes))

  def addOrRemoveOfficesOfTransit: Option[Link] = buildLink(OfficesOfTransitSection) {
    Link(
      id = "add-or-remove-offices-of-transit",
      text = messages("routeDetails.checkYourAnswers.transit.addOrRemove"),
      href = controllers.transit.routes.AddAnotherOfficeOfTransitController.onPageLoad(userAnswers.lrn, mode).url
    )
  }

  def listItems: Seq[Either[ListItem, ListItem]] =
    buildListItems(OfficesOfTransitSection) {
      index =>
        val removeRoute: Option[Call] = if (userAnswers.get(AddOfficeOfTransitYesNoPage).isEmpty && index.isFirst) {
          None
        } else {
          Some(routes.ConfirmRemoveOfficeOfTransitController.onPageLoad(userAnswers.lrn, mode, index))
        }

        buildListItem[OfficeOfTransitDomain](
          nameWhenComplete = _.label,
          nameWhenInProgress = userAnswers.get(OfficeOfTransitCountryPage(index)).map(_.toString),
          removeRoute = removeRoute
        )(OfficeOfTransitDomain.userAnswersReader(index, ctcCountryCodes, customsSecurityAgreementAreaCountryCodes))
    }
}
