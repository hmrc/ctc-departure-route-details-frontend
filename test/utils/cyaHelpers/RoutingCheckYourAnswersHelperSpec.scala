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

package utils.cyaHelpers

import base.SpecBase
import controllers.routing.index.{routes => indexRoutes}
import controllers.routing.{routes => routingRoutes}
import generators.Generators
import models.journeyDomain.UserAnswersReader
import models.journeyDomain.routing.CountryOfRoutingDomain
import models.reference.{Country, CustomsOffice, SpecificCircumstanceIndicator}
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.routing.index.CountryOfRoutingPage
import pages.routing.{AddCountryOfRoutingYesNoPage, BindingItineraryPage, CountryOfDestinationPage, OfficeOfDestinationPage}
import pages.sections.routing.CountryOfRoutingSection
import pages.{AddSpecificCircumstanceIndicatorYesNoPage, SpecificCircumstanceIndicatorPage}
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.Aliases.{Key, SummaryListRow, Value}
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.html.components.{ActionItem, Actions}
import utils.cyaHelpers.routing.RoutingCheckYourAnswersHelper
import viewModels.ListItem

class RoutingCheckYourAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "RoutingCheckYourAnswersHelper" - {

    "addSpecificCircumstanceIndicator" - {
      "must return None" - {
        "when AddSpecificCircumstanceIndicatorYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new RoutingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addSpecificCircumstanceIndicatorYesNo
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when AddSpecificCircumstanceIndicatorYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddSpecificCircumstanceIndicatorYesNoPage, true)

              val helper = new RoutingCheckYourAnswersHelper(answers, mode)
              val result = helper.addSpecificCircumstanceIndicatorYesNo

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add a specific circumstance indicator?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.routes.AddSpecificCircumstanceIndicatorYesNoController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you want to add a specific circumstance indicator"),
                          attributes = Map("id" -> "change-add-specific-circumstance-indicator")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "specificCircumstanceIndicator" - {
      "must return None" - {
        "when SpecificCircumstanceIndicatorPage undefined at index" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new RoutingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.specificCircumstanceIndicator
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when SpecificCircumstanceIndicatorPage defined" in {
          forAll(arbitrary[SpecificCircumstanceIndicator], arbitrary[Mode]) {
            (specificCircumstanceIndicator, mode) =>
              val answers = emptyUserAnswers.setValue(SpecificCircumstanceIndicatorPage, specificCircumstanceIndicator)

              val helper = new RoutingCheckYourAnswersHelper(answers, mode)
              val result = helper.specificCircumstanceIndicator

              result.value mustEqual
                SummaryListRow(
                  key = Key("Specific circumstance indicator".toText),
                  value = Value(s"$specificCircumstanceIndicator".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.routes.SpecificCircumstanceIndicatorController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("specific circumstance indicator"),
                          attributes = Map("id" -> "change-specific-circumstance-indicator")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "countryOfDestination" - {
      "must return None" - {
        "when CountryOfDestinationPage undefined at index" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new RoutingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.countryOfDestination
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when CountryOfDestinationPage defined" in {
          forAll(arbitrary[Country], arbitrary[Mode]) {
            (country, mode) =>
              val answers = emptyUserAnswers.setValue(CountryOfDestinationPage, country)

              val helper = new RoutingCheckYourAnswersHelper(answers, mode)
              val result = helper.countryOfDestination

              result.value mustEqual
                SummaryListRow(
                  key = Key("Office of destination’s country".toText),
                  value = Value(country.toString.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routingRoutes.CountryOfDestinationController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("office of destination’s country"),
                          attributes = Map("id" -> "change-office-of-destination-country")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "officeOfDestination" - {
      "must return None" - {
        "when OfficeOfDestinationPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new RoutingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.officeOfDestination
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when OfficeOfDestinationPage defined" in {
          forAll(arbitrary[CustomsOffice], arbitrary[Mode]) {
            (customsOffice, mode) =>
              val answers = emptyUserAnswers.setValue(OfficeOfDestinationPage, customsOffice)

              val helper = new RoutingCheckYourAnswersHelper(answers, mode)
              val result = helper.officeOfDestination

              result.value mustEqual
                SummaryListRow(
                  key = Key("Office of destination".toText),
                  value = Value(s"$customsOffice".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routingRoutes.OfficeOfDestinationController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("office of destination"),
                          attributes = Map("id" -> "change-office-of-destination")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "bindingItinerary" - {
      "must return None" - {
        "when BindingItineraryPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new RoutingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.bindingItinerary
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when BindingItineraryPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(BindingItineraryPage, true)

              val helper = new RoutingCheckYourAnswersHelper(answers, mode)
              val result = helper.bindingItinerary

              result.value mustEqual
                SummaryListRow(
                  key = Key("Are you using a binding itinerary?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routingRoutes.BindingItineraryController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you are using a binding itinerary"),
                          attributes = Map("id" -> "change-binding-itinerary")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "addCountryOfRouting" - {
      "must return None" - {
        "when AddCountryOfRoutingYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new RoutingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addCountryOfRouting
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when AddCountryOfRoutingYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddCountryOfRoutingYesNoPage, true)

              val helper = new RoutingCheckYourAnswersHelper(answers, mode)
              val result = helper.addCountryOfRouting

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add a country to the transit route?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routingRoutes.AddCountryOfRoutingYesNoController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you want to add a country to the transit route"),
                          attributes = Map("id" -> "change-add-country-of-routing")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "countryOfRouting" - {
      "must return None" - {
        "when country of routing is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new RoutingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.countryOfRouting(index)
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when country of routing is defined" in {
          forAll(arbitraryCountryOfRoutingAnswers(emptyUserAnswers, index), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val countryOfRouting = UserAnswersReader[CountryOfRoutingDomain](
                CountryOfRoutingDomain.userAnswersReader(index).apply(Nil)
              ).run(userAnswers).value.value

              val helper = new RoutingCheckYourAnswersHelper(userAnswers, mode)
              val result = helper.countryOfRouting(index).get

              result.key.value mustEqual "Country 1"
              result.value.value mustEqual countryOfRouting.country.toString
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual indexRoutes.CountryOfRoutingController.onPageLoad(userAnswers.lrn, mode, index).url
              action.visuallyHiddenText.get mustEqual "country 1 in the transit route"
              action.id mustEqual "change-country-of-routing-1"
          }
        }
      }
    }

    "addOrRemoveCountriesOfRouting" - {
      "must return None" - {
        "when countries of routing array is empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new RoutingCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addOrRemoveCountriesOfRouting
              result must not be defined
          }
        }
      }

      "must return Some(Link)" - {
        "when countries of routing array is non-empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(CountryOfRoutingSection(Index(0)), Json.obj("foo" -> "bar"))
              val helper  = new RoutingCheckYourAnswersHelper(answers, mode)
              val result  = helper.addOrRemoveCountriesOfRouting.get

              result.id mustEqual "add-or-remove-transit-route-countries"
              result.text mustEqual "Add or remove transit route countries"
              result.href mustEqual routingRoutes.AddAnotherCountryOfRoutingController.onPageLoad(answers.lrn, mode).url
          }
        }
      }
    }

    "listItems" - {
      "must return list items" in {
        val mode     = arbitrary[Mode].sample.value
        val country1 = arbitrary[Country].sample.value
        val country2 = arbitrary[Country].sample.value

        val answers = emptyUserAnswers
          .setValue(CountryOfRoutingPage(Index(0)), country1)
          .setValue(CountryOfRoutingPage(Index(1)), country2)

        val helper = new RoutingCheckYourAnswersHelper(answers, mode)
        helper.listItems mustEqual Seq(
          Right(
            ListItem(
              name = country1.toString,
              changeUrl = indexRoutes.CountryOfRoutingController.onPageLoad(answers.lrn, mode, Index(0)).url,
              removeUrl = Some(indexRoutes.RemoveCountryOfRoutingYesNoController.onPageLoad(answers.lrn, mode, Index(0)).url)
            )
          ),
          Right(
            ListItem(
              name = country2.toString,
              changeUrl = indexRoutes.CountryOfRoutingController.onPageLoad(answers.lrn, mode, Index(1)).url,
              removeUrl = Some(indexRoutes.RemoveCountryOfRoutingYesNoController.onPageLoad(answers.lrn, mode, Index(1)).url)
            )
          )
        )
      }
    }
  }
}
