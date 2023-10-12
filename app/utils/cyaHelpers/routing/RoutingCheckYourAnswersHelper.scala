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

package utils.cyaHelpers.routing

import controllers.routing.index.routes
import config.FrontendAppConfig
import models.journeyDomain.routing.CountryOfRoutingDomain
import models.reference.{Country, CustomsOffice, SpecificCircumstanceIndicator}
import models.{Index, Mode, UserAnswers}
import pages.{AddSpecificCircumstanceIndicatorYesNoPage, SpecificCircumstanceIndicatorPage}
import pages.routing.index.CountryOfRoutingPage
import pages.routing.{AddCountryOfRoutingYesNoPage, BindingItineraryPage, CountryOfDestinationPage, OfficeOfDestinationPage}
import pages.sections.routing.CountriesOfRoutingSection
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.Aliases.SummaryListRow
import utils.cyaHelpers.AnswersHelper
import viewModels.{Link, ListItem}

class RoutingCheckYourAnswersHelper(userAnswers: UserAnswers, mode: Mode)(implicit messages: Messages, config: FrontendAppConfig)
    extends AnswersHelper(userAnswers, mode) {

  def addSpecificCircumstanceIndicatorYesNo: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddSpecificCircumstanceIndicatorYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "addSpecificCircumstanceIndicatorYesNo",
    id = Some("change-add-specific-circumstance-indicator")
  )

  def specificCircumstanceIndicator: Option[SummaryListRow] = getAnswerAndBuildRow[SpecificCircumstanceIndicator](
    page = SpecificCircumstanceIndicatorPage,
    formatAnswer = formatAsText,
    prefix = "specificCircumstanceIndicator",
    id = Some("change-specific-circumstance-indicator")
  )

  def countryOfDestination: Option[SummaryListRow] = getAnswerAndBuildRow[Country](
    page = CountryOfDestinationPage,
    formatAnswer = formatAsCountry,
    prefix = "routing.countryOfDestination",
    id = Some("change-office-of-destination-country")
  )

  def officeOfDestination: Option[SummaryListRow] = getAnswerAndBuildRow[CustomsOffice](
    page = OfficeOfDestinationPage,
    formatAnswer = formatAsText,
    prefix = "routing.officeOfDestination",
    id = Some("change-office-of-destination")
  )

  def bindingItinerary: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = BindingItineraryPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "routing.bindingItinerary",
    id = Some("change-binding-itinerary")
  )

  def addCountryOfRouting: Option[SummaryListRow] = getAnswerAndBuildRow[Boolean](
    page = AddCountryOfRoutingYesNoPage,
    formatAnswer = formatAsYesOrNo,
    prefix = "routing.addCountryOfRoutingYesNo",
    id = Some("change-add-country-of-routing")
  )

  def countriesOfRouting: Seq[SummaryListRow] =
    getAnswersAndBuildSectionRows(CountriesOfRoutingSection)(countryOfRouting)

  def countryOfRouting(index: Index): Option[SummaryListRow] = getAnswerAndBuildSectionRow[CountryOfRoutingDomain](
    formatAnswer = _.country.toString.toText,
    prefix = "checkYourAnswers.routing.countryOfRouting",
    id = Some(s"change-country-of-routing-${index.display}"),
    args = index.display
  )(CountryOfRoutingDomain.userAnswersReader(index))

  def addOrRemoveCountriesOfRouting: Option[Link] = buildLink(CountriesOfRoutingSection) {
    Link(
      id = "add-or-remove-transit-route-countries",
      text = messages("checkYourAnswers.routing.addOrRemove"),
      href = controllers.routing.routes.AddAnotherCountryOfRoutingController.onPageLoad(userAnswers.lrn, mode).url
    )
  }

  def listItems: Seq[Either[ListItem, ListItem]] =
    buildListItems(CountriesOfRoutingSection) {
      index =>
        buildListItem[CountryOfRoutingDomain](
          nameWhenComplete = _.country.toString,
          nameWhenInProgress = userAnswers.get(CountryOfRoutingPage(index)).map(_.toString),
          removeRoute = Some(routes.RemoveCountryOfRoutingYesNoController.onPageLoad(userAnswers.lrn, mode, index))
        )(CountryOfRoutingDomain.userAnswersReader(index))
    }
}
