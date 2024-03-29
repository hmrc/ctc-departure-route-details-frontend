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

import config.FrontendAppConfig
import models.reference.{Country, CustomsOffice}
import models.{DateTime, Index, Mode, UserAnswers}
import pages.transit.index.{AddOfficeOfTransitETAYesNoPage, OfficeOfTransitCountryPage, OfficeOfTransitETAPage, OfficeOfTransitPage}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import utils.cyaHelpers.AnswersHelper

class OfficeOfTransitCheckYourAnswersHelper(userAnswers: UserAnswers, mode: Mode, index: Index)(implicit messages: Messages, config: FrontendAppConfig)
    extends AnswersHelper(userAnswers, mode) {

  def officeOfTransitCountry: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = OfficeOfTransitCountryPage(index),
    formatAnswer = formatAsCountry,
    prefix = "transit.index.officeOfTransitCountry",
    id = Some("change-office-of-transit-country")
  )

  def officeOfTransit: Option[SummaryListRow] = getAnswerAndBuildRow[CustomsOffice](
    page = OfficeOfTransitPage(index),
    formatAnswer = formatAsText,
    prefix = "transit.index.officeOfTransit",
    id = Some("change-office-of-transit")
  )

  def addOfficeOfTransitETA: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddOfficeOfTransitETAYesNoPage(index),
    formatAnswer = formatAsYesOrNo,
    prefix = "transit.index.addOfficeOfTransitETAYesNo",
    id = Some("change-office-of-transit-add-eta")
  )

  def officeOfTransitETA: Option[SummaryListRow] = getAnswerAndBuildRow[DateTime](
    page = OfficeOfTransitETAPage(index),
    formatAnswer = formatAsDateTime,
    prefix = "transit.index.officeOfTransitETA",
    id = Some("change-office-of-transit-eta")
  )

}
