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
import config.Constants.SecurityType.*
import controllers.transit.index.routes as indexRoutes
import generators.Generators
import models.journeyDomain.UserAnswersReader
import models.journeyDomain.transit.OfficeOfTransitDomain
import models.reference.{Country, CustomsOffice}
import models.{Index, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.{OfficeOfDepartureInCL112Page, OfficeOfDeparturePage, SecurityDetailsTypePage}
import pages.routing.{OfficeOfDestinationInCL112Page, OfficeOfDestinationPage}
import pages.sections.transit.OfficeOfTransitSection
import pages.transit.index.{AddOfficeOfTransitETAYesNoPage, OfficeOfTransitCountryPage, OfficeOfTransitPage}
import pages.transit.{AddOfficeOfTransitYesNoPage, T2DeclarationTypeYesNoPage}
import play.api.libs.json.Json
import uk.gov.hmrc.govukfrontend.views.Aliases.*
import uk.gov.hmrc.govukfrontend.views.html.components.implicits.*
import utils.cyaHelpers.transit.TransitCheckYourAnswersHelper
import viewModels.ListItem

class TransitCheckYourAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "TransitCheckYourAnswersHelper" - {

    "includesT2Declarations" - {
      "must return None" - {
        "when T2DeclarationTypeYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransitCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.includesT2Declarations
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when T2DeclarationTypeYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(T2DeclarationTypeYesNoPage, true)

              val helper = new TransitCheckYourAnswersHelper(answers, mode)
              val result = helper.includesT2Declarations

              result.value mustEqual
                SummaryListRow(
                  key = Key("Does the transit include any T2 declarations?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.transit.routes.T2DeclarationTypeYesNoController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if the transit includes any T2 declarations"),
                          attributes = Map("id" -> "change-includes-t2-declarations")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "addOfficeOfTransit" - {
      "must return None" - {
        "when AddOfficeOfTransitYesNoPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransitCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addOfficeOfTransit
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when AddOfficeOfTransitYesNoPage defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddOfficeOfTransitYesNoPage, true)

              val helper = new TransitCheckYourAnswersHelper(answers, mode)
              val result = helper.addOfficeOfTransit

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add an office of transit?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = controllers.transit.routes.AddOfficeOfTransitYesNoController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you want to add an office of transit"),
                          attributes = Map("id" -> "change-add-office-of-transit")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "officeOfTransit" - {
      "must return None" - {
        "when office of transit is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransitCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.officeOfTransit(index)
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when office of transit is defined" in {
          forAll(arbitraryOfficeOfTransitAnswers(emptyUserAnswers, index), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val officeOfExit = UserAnswersReader[OfficeOfTransitDomain](
                OfficeOfTransitDomain.userAnswersReader(index).apply(Nil)
              ).run(userAnswers).value.value

              val helper = new TransitCheckYourAnswersHelper(userAnswers, mode)
              val result = helper.officeOfTransit(index).get

              result.key.value mustEqual "Office of transit 1"
              result.value.value mustEqual officeOfExit.label
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual indexRoutes.CheckOfficeOfTransitAnswersController.onPageLoad(userAnswers.lrn, mode, index).url
              action.visuallyHiddenText.get mustEqual "office of transit 1"
              action.id mustEqual "change-office-of-transit-1"
          }
        }
      }
    }

    "addOrRemoveOfficesOfTransit" - {
      "must return None" - {
        "when offices of transit array is empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new TransitCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addOrRemoveOfficesOfTransit
              result must not be defined
          }
        }
      }

      "must return Some(Link)" - {
        "when offices of transit array is non-empty" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(OfficeOfTransitSection(Index(0)), Json.obj("foo" -> "bar"))
              val helper  = new TransitCheckYourAnswersHelper(answers, mode)
              val result  = helper.addOrRemoveOfficesOfTransit.get

              result.id mustEqual "add-or-remove-offices-of-transit"
              result.text mustEqual "Add or remove offices of transit"
              result.href mustEqual controllers.transit.routes.AddAnotherOfficeOfTransitController.onPageLoad(answers.lrn, mode).url
          }
        }
      }
    }

    "listItems" - {
      "must return list items" - {
        "when multiple" in {
          val mode = arbitrary[Mode].sample.value

          val country1 = arbitrary[Country].sample.value
          val country2 = arbitrary[Country].sample.value
          val country3 = arbitrary[Country].sample.value

          def customsOffice = arbitrary[CustomsOffice].sample.value

          val customsOffice1 = customsOffice.copy(id = country1.code.code)
          val customsOffice2 = customsOffice.copy(id = country2.code.code)

          val answers = emptyUserAnswers
            .setValue(OfficeOfDeparturePage, customsOffice)
            .setValue(OfficeOfDepartureInCL112Page, true)
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(OfficeOfDestinationPage, customsOffice1)
            .setValue(OfficeOfDestinationInCL112Page, true)
            .setValue(OfficeOfTransitCountryPage(Index(0)), country1)
            .setValue(OfficeOfTransitPage(Index(0)), customsOffice1)
            .setValue(AddOfficeOfTransitETAYesNoPage(Index(0)), false)
            .setValue(OfficeOfTransitCountryPage(Index(1)), country2)
            .setValue(OfficeOfTransitPage(Index(1)), customsOffice2)
            .setValue(AddOfficeOfTransitETAYesNoPage(Index(1)), false)
            .setValue(OfficeOfTransitCountryPage(Index(2)), country3)

          val helper = new TransitCheckYourAnswersHelper(answers, mode)(messages = messages, config = frontendAppConfig)
          helper.listItems mustEqual Seq(
            Right(
              ListItem(
                name = s"$customsOffice1",
                changeUrl = indexRoutes.CheckOfficeOfTransitAnswersController.onPageLoad(lrn, mode, Index(0)).url,
                removeUrl = Some(indexRoutes.ConfirmRemoveOfficeOfTransitController.onPageLoad(lrn, mode, Index(0)).url)
              )
            ),
            Right(
              ListItem(
                name = s"$country2 - $customsOffice2",
                changeUrl = indexRoutes.CheckOfficeOfTransitAnswersController.onPageLoad(lrn, mode, Index(1)).url,
                removeUrl = Some(indexRoutes.ConfirmRemoveOfficeOfTransitController.onPageLoad(lrn, mode, Index(1)).url)
              )
            ),
            Left(
              ListItem(
                name = s"$country3",
                changeUrl = indexRoutes.OfficeOfTransitController.onPageLoad(lrn, mode, Index(2)).url,
                removeUrl = Some(indexRoutes.ConfirmRemoveOfficeOfTransitController.onPageLoad(lrn, mode, Index(2)).url)
              )
            )
          )
        }

        "when one" - {
          "in progress" - {
            "and it can be removed" in {
              val mode = arbitrary[Mode].sample.value

              val country1 = arbitrary[Country].sample.value

              def customsOffice = arbitrary[CustomsOffice].sample.value

              val customsOffice1 = customsOffice.copy(id = country1.code.code)

              val answers = emptyUserAnswers
                .setValue(OfficeOfDeparturePage, customsOffice1)
                .setValue(OfficeOfDepartureInCL112Page, true)
                .setValue(SecurityDetailsTypePage, NoSecurityDetails)
                .setValue(OfficeOfDestinationPage, customsOffice1)
                .setValue(OfficeOfDestinationInCL112Page, true)
                .setValue(AddOfficeOfTransitYesNoPage, true)
                .setValue(OfficeOfTransitCountryPage(Index(0)), country1)
                .setValue(OfficeOfTransitPage(Index(0)), customsOffice1)

              val helper = new TransitCheckYourAnswersHelper(answers, mode)(messages = messages, config = frontendAppConfig)
              helper.listItems mustEqual Seq(
                Left(
                  ListItem(
                    name = s"$country1",
                    changeUrl = indexRoutes.AddOfficeOfTransitETAYesNoController.onPageLoad(lrn, mode, Index(0)).url,
                    removeUrl = Some(indexRoutes.ConfirmRemoveOfficeOfTransitController.onPageLoad(lrn, mode, Index(0)).url)
                  )
                )
              )
            }

            "and it cannot be removed" in {
              val mode = arbitrary[Mode].sample.value

              val country1 = arbitrary[Country].sample.value

              def customsOffice = arbitrary[CustomsOffice].sample.value

              val customsOffice1 = customsOffice.copy(id = country1.code.code)

              val answers = emptyUserAnswers
                .setValue(OfficeOfDeparturePage, customsOffice)
                .setValue(OfficeOfDepartureInCL112Page, true)
                .setValue(SecurityDetailsTypePage, NoSecurityDetails)
                .setValue(OfficeOfDestinationPage, customsOffice1)
                .setValue(OfficeOfDestinationInCL112Page, true)
                .setValue(OfficeOfTransitCountryPage(Index(0)), country1)
                .setValue(OfficeOfTransitPage(Index(0)), customsOffice1)

              val helper = new TransitCheckYourAnswersHelper(answers, mode)(messages = messages, config = frontendAppConfig)
              helper.listItems mustEqual Seq(
                Left(
                  ListItem(
                    name = s"$country1",
                    changeUrl = indexRoutes.AddOfficeOfTransitETAYesNoController.onPageLoad(lrn, mode, Index(0)).url,
                    removeUrl = None
                  )
                )
              )
            }
          }

          "completed" - {
            "and it can be removed" in {
              val mode = arbitrary[Mode].sample.value

              val country1 = arbitrary[Country].sample.value

              def customsOffice = arbitrary[CustomsOffice].sample.value

              val customsOffice1 = customsOffice.copy(id = country1.code.code)

              val answers = emptyUserAnswers
                .setValue(OfficeOfDeparturePage, customsOffice1)
                .setValue(OfficeOfDepartureInCL112Page, true)
                .setValue(SecurityDetailsTypePage, NoSecurityDetails)
                .setValue(OfficeOfDestinationPage, customsOffice1)
                .setValue(OfficeOfDestinationInCL112Page, true)
                .setValue(AddOfficeOfTransitYesNoPage, true)
                .setValue(OfficeOfTransitCountryPage(Index(0)), country1)
                .setValue(OfficeOfTransitPage(Index(0)), customsOffice1)
                .setValue(AddOfficeOfTransitETAYesNoPage(Index(0)), false)

              val helper = new TransitCheckYourAnswersHelper(answers, mode)(messages = messages, config = frontendAppConfig)
              helper.listItems mustEqual Seq(
                Right(
                  ListItem(
                    name = s"$customsOffice1",
                    changeUrl = indexRoutes.CheckOfficeOfTransitAnswersController.onPageLoad(lrn, mode, Index(0)).url,
                    removeUrl = Some(indexRoutes.ConfirmRemoveOfficeOfTransitController.onPageLoad(lrn, mode, Index(0)).url)
                  )
                )
              )
            }

            "and it cannot be removed" in {
              val mode = arbitrary[Mode].sample.value

              val country1 = arbitrary[Country].sample.value

              def customsOffice = arbitrary[CustomsOffice].sample.value

              val customsOffice1 = customsOffice.copy(id = country1.code.code)

              val answers = emptyUserAnswers
                .setValue(OfficeOfDeparturePage, customsOffice)
                .setValue(OfficeOfDepartureInCL112Page, true)
                .setValue(SecurityDetailsTypePage, NoSecurityDetails)
                .setValue(OfficeOfDestinationPage, customsOffice1)
                .setValue(OfficeOfDestinationInCL112Page, true)
                .setValue(OfficeOfTransitCountryPage(Index(0)), country1)
                .setValue(OfficeOfTransitPage(Index(0)), customsOffice1)
                .setValue(AddOfficeOfTransitETAYesNoPage(Index(0)), false)

              val helper = new TransitCheckYourAnswersHelper(answers, mode)(messages = messages, config = frontendAppConfig)
              helper.listItems mustEqual Seq(
                Right(
                  ListItem(
                    name = s"$customsOffice1",
                    changeUrl = indexRoutes.CheckOfficeOfTransitAnswersController.onPageLoad(lrn, mode, Index(0)).url,
                    removeUrl = None
                  )
                )
              )
            }
          }
        }
      }
    }
  }
}
