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

package utils.cyaHelpers.exit

import config.FrontendAppConfig
import controllers.exit.index.routes
import models.journeyDomain.exit.OfficeOfExitDomain
import models.{Index, Mode, UserAnswers}
import pages.exit.AddCustomsOfficeOfExitYesNoPage
import pages.exit.index.OfficeOfExitCountryPage
import pages.sections.exit.OfficesOfExitSection
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import utils.cyaHelpers.AnswersHelper
import viewModels.{Link, ListItem}

class ExitCheckYourAnswersHelper(
  userAnswers: UserAnswers,
  mode: Mode
)(implicit messages: Messages, config: FrontendAppConfig)
    extends AnswersHelper(userAnswers, mode) {

  def officesOfExit: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(OfficesOfExitSection)(officeOfExit)

  def officeOfExit(index: Index): Option[SummaryListRow] = getAnswerAndBuildSectionRow[OfficeOfExitDomain](
    formatAnswer = _.label.toText,
    prefix = "checkYourAnswers.exit.officeOfExit",
    id = Some(s"change-office-of-exit-${index.display}"),
    args = index.display
  )(OfficeOfExitDomain.userAnswersReader(index))

  def addOrRemoveOfficesOfExit: Option[Link] = buildLink(OfficesOfExitSection) {
    Link(
      id = "add-or-remove-offices-of-exit",
      text = messages("checkYourAnswers.exit.addOrRemove"),
      href = controllers.exit.routes.AddAnotherOfficeOfExitController.onPageLoad(userAnswers.lrn, mode).url
    )
  }

  def addOfficeOfExitYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddCustomsOfficeOfExitYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "exit.AddCustomsOfficeOfExitYesNo",
    id = Some("change-add-customs-office-of-exit")
  )

  def listItems: Seq[Either[ListItem, ListItem]] =
    buildListItems(OfficesOfExitSection) {
      index =>
        buildListItem[OfficeOfExitDomain](
          nameWhenComplete = _.label,
          nameWhenInProgress = userAnswers.get(OfficeOfExitCountryPage(index)).map(_.toString),
          removeRoute = Some(routes.ConfirmRemoveOfficeOfExitController.onPageLoad(userAnswers.lrn, index, mode))
        )(OfficeOfExitDomain.userAnswersReader(index))
    }
}
