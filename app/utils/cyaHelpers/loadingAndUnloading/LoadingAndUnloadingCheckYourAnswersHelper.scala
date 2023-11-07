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

package utils.cyaHelpers.loadingAndUnloading

import config.FrontendAppConfig
import models.reference.Country
import models.{Mode, UserAnswers}
import pages.loadingAndUnloading
import pages.loadingAndUnloading._
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import utils.cyaHelpers.AnswersHelper

class LoadingAndUnloadingCheckYourAnswersHelper(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages, config: FrontendAppConfig)
    extends AnswersHelper(userAnswers, mode) {

  def addLoading: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = loadingAndUnloading.AddPlaceOfLoadingYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "loadingAndUnloading.addPlaceOfLoadingYesNo",
    id = Some("change-add-loading")
  )

  def addLoadingUnLocode: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = loading.AddUnLocodeYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "loadingAndUnloading.loading.addUnLocodeYesNo",
    id = Some("change-add-loading-un-locode")
  )

  def loadingUnLocode: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = loading.UnLocodePage,
    formatAnswer = formatAsText,
    prefix = "loadingAndUnloading.loading.unLocode",
    id = Some("change-loading-un-locode")
  )

  def addLoadingCountryAndLocation: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = loading.AddExtraInformationYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "loadingAndUnloading.loading.addExtraInformationYesNo",
    id = Some("change-add-loading-country-and-location")
  )

  def loadingCountry: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = loading.CountryPage,
    formatAnswer = formatAsCountry,
    prefix = "loadingAndUnloading.loading.country",
    id = Some("change-loading-country")
  )

  def loadingLocation: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = loading.LocationPage,
    formatAnswer = formatAsText,
    prefix = "loadingAndUnloading.loading.location",
    id = Some("change-loading-location")
  )

  def addPlaceOfUnloading: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = loadingAndUnloading.AddPlaceOfUnloadingPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "loadingAndUnloading.addPlaceOfUnloading",
    id = Some("change-add-place-of-unloading")
  )

  def addUnloadingUnLocode: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = unloading.UnLocodeYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "loadingAndUnloading.unloading.addUnLocodeYesNo",
    id = Some("change-add-unloading-un-locode")
  )

  def unloadingUnLocode: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = unloading.UnLocodePage,
    formatAnswer = formatAsText,
    prefix = "loadingAndUnloading.unloading.unLocode",
    id = Some("change-unloading-un-locode")
  )

  def addUnloadingCountryAndLocation: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = unloading.AddExtraInformationYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "loadingAndUnloading.unloading.addExtraInformationYesNo",
    id = Some("change-add-unloading-country-and-location")
  )

  def unloadingCountry: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = unloading.CountryPage,
    formatAnswer = formatAsCountry,
    prefix = "loadingAndUnloading.unloading.country",
    id = Some("change-unloading-country")
  )

  def unloadingLocation: Option[SummaryListRow] = getAnswerAndBuildRow[String](
    page = unloading.LocationPage,
    formatAnswer = formatAsText,
    prefix = "loadingAndUnloading.unloading.location",
    id = Some("change-unloading-location")
  )
}
