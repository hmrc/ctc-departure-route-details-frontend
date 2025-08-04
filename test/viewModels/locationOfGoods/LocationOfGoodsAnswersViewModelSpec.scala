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

package viewModels.locationOfGoods

import base.{AppWithDefaultMockFixtures, SpecBase}
import config.Constants.LocationOfGoodsIdentifier.*
import generators.Generators
import models.Mode
import models.reference.LocationOfGoodsIdentification
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.locationOfGoods.{AddContactYesNoPage, AddIdentifierYesNoPage, AddLocationOfGoodsPage, IdentificationPage}
import viewModels.locationOfGoods.LocationOfGoodsAnswersViewModel.LocationOfGoodsAnswersViewModelProvider

class LocationOfGoodsAnswersViewModelSpec extends SpecBase with AppWithDefaultMockFixtures with ScalaCheckPropertyChecks with Generators {

  private val baseAnswers = emptyUserAnswers.setValue(AddLocationOfGoodsPage, true)

  "apply" - {
    "when 'V' customs office" - {
      val qualifier = CustomsOfficeIdentifier

      "must return 4 rows" in {
        val initialAnswers = baseAnswers
          .setValue(IdentificationPage, LocationOfGoodsIdentification(qualifier, "test"))

        forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
          (userAnswers, mode) =>
            val viewModelProvider = new LocationOfGoodsAnswersViewModelProvider()
            val section           = viewModelProvider.apply(userAnswers, mode).section
            section.rows.size mustEqual 4
            section.sectionTitle.get mustEqual "Route details - Location of goods"
        }
      }
    }

    "when 'X' EORI number" - {
      val qualifier = EoriNumberIdentifier

      "when an additional identifier and a contact have been provided" - {
        "must return 9 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, LocationOfGoodsIdentification(qualifier, "test"))
            .setValue(AddIdentifierYesNoPage, true)
            .setValue(AddContactYesNoPage, true)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = new LocationOfGoodsAnswersViewModelProvider()
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustEqual 9
              section.sectionTitle.get mustEqual "Route details - Location of goods"
          }
        }
      }

      "when neither an additional identifier nor a contact have been provided" - {
        "must return 6 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, LocationOfGoodsIdentification(qualifier, "test"))
            .setValue(AddIdentifierYesNoPage, false)
            .setValue(AddContactYesNoPage, false)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = new LocationOfGoodsAnswersViewModelProvider()
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustEqual 6
              section.sectionTitle.get mustEqual "Route details - Location of goods"
          }
        }
      }
    }

    "when 'Y' authorisations number" - {
      val qualifier = AuthorisationNumberIdentifier

      "when an additional identifier and a contact have been provided" - {
        "must return 9 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, LocationOfGoodsIdentification(qualifier, "test"))
            .setValue(AddIdentifierYesNoPage, true)
            .setValue(AddContactYesNoPage, true)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = new LocationOfGoodsAnswersViewModelProvider()
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustEqual 9
              section.sectionTitle.get mustEqual "Route details - Location of goods"
          }
        }
      }

      "when neither an additional identifier nor a contact have been provided" - {
        "must return 6 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, LocationOfGoodsIdentification(qualifier, "test"))
            .setValue(AddIdentifierYesNoPage, false)
            .setValue(AddContactYesNoPage, false)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = new LocationOfGoodsAnswersViewModelProvider()
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustEqual 6
              section.sectionTitle.get mustEqual "Route details - Location of goods"
          }
        }
      }
    }

    "when 'W' coordinates" - {
      val qualifier = CoordinatesIdentifier

      "when a contact has been provided" - {
        "must return 7 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, LocationOfGoodsIdentification(qualifier, "test"))
            .setValue(AddContactYesNoPage, true)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = new LocationOfGoodsAnswersViewModelProvider()
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustEqual 7
              section.sectionTitle.get mustEqual "Route details - Location of goods"
          }
        }
      }

      "when a contact has not been provided" - {
        "must return 5 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, LocationOfGoodsIdentification(qualifier, "test"))
            .setValue(AddContactYesNoPage, false)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = new LocationOfGoodsAnswersViewModelProvider()
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustEqual 5
              section.sectionTitle.get mustEqual "Route details - Location of goods"
          }
        }
      }
    }

    "when 'U' UN-LOCODE" - {
      val qualifier = UnlocodeIdentifier

      "when a contact has been provided" - {
        "must return 7 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, LocationOfGoodsIdentification(qualifier, "test"))
            .setValue(AddContactYesNoPage, true)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = new LocationOfGoodsAnswersViewModelProvider()
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustEqual 7
              section.sectionTitle.get mustEqual "Route details - Location of goods"
          }
        }
      }

      "when a contact has not been provided" - {
        "must return 5 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, LocationOfGoodsIdentification(qualifier, "test"))
            .setValue(AddContactYesNoPage, false)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = new LocationOfGoodsAnswersViewModelProvider()
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustEqual 5
              section.sectionTitle.get mustEqual "Route details - Location of goods"
          }
        }
      }
    }

    "when 'Z' address" - {
      val qualifier = AddressIdentifier

      "when a contact has been provided" - {
        "must return 8 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, LocationOfGoodsIdentification(qualifier, "test"))
            .setValue(AddContactYesNoPage, true)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = new LocationOfGoodsAnswersViewModelProvider()
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustEqual 8
              section.sectionTitle.get mustEqual "Route details - Location of goods"
          }
        }
      }

      "when a contact has not been provided" - {
        "must return 6 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, LocationOfGoodsIdentification(qualifier, "test"))
            .setValue(AddContactYesNoPage, false)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = new LocationOfGoodsAnswersViewModelProvider()
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustEqual 6
              section.sectionTitle.get mustEqual "Route details - Location of goods"
          }
        }
      }
    }
  }
}
