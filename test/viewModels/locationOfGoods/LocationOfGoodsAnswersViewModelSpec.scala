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

import base.SpecBase
import config.Constants._
import generators.Generators
import models.{LocationOfGoodsIdentification, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.locationOfGoods.{AddContactYesNoPage, AddIdentifierYesNoPage, AddLocationOfGoodsPage, IdentificationPage}
import viewModels.locationOfGoods.LocationOfGoodsAnswersViewModel.LocationOfGoodsAnswersViewModelProvider

class LocationOfGoodsAnswersViewModelSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private val baseAnswers = emptyUserAnswers.setValue(AddLocationOfGoodsPage, true)

  "apply" - {
    "when 'V' customs office" - {
      val qualifier = LocationOfGoodsIdentification(CustomsOfficeIdentifier, "CustomsOfficeIdentifier")

      "must return 4 rows" in {
        val initialAnswers = baseAnswers
          .setValue(IdentificationPage, qualifier)

        forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
          (userAnswers, mode) =>
            val viewModelProvider = injector.instanceOf[LocationOfGoodsAnswersViewModelProvider]
            val section           = viewModelProvider.apply(userAnswers, mode).section
            section.rows.size mustBe 4
            section.sectionTitle.get mustBe "Location of goods"
        }
      }
    }

    "when 'X' EORI number" - {
      val qualifier = LocationOfGoodsIdentification(EoriNumberIdentifier, "EoriNumber")

      "when an additional identifier and a contact have been provided" - {
        "must return 9 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, qualifier)
            .setValue(AddIdentifierYesNoPage, true)
            .setValue(AddContactYesNoPage, true)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = injector.instanceOf[LocationOfGoodsAnswersViewModelProvider]
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustBe 9
              section.sectionTitle.get mustBe "Location of goods"
          }
        }
      }

      "when neither an additional identifier nor a contact have been provided" - {
        "must return 6 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, qualifier)
            .setValue(AddIdentifierYesNoPage, false)
            .setValue(AddContactYesNoPage, false)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = injector.instanceOf[LocationOfGoodsAnswersViewModelProvider]
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustBe 6
              section.sectionTitle.get mustBe "Location of goods"
          }
        }
      }
    }

    "when 'Y' authorisations number" - {
      val qualifier = LocationOfGoodsIdentification(AuthorisationNumberIdentifier, "AuthorisationNumberIdentifier")

      "when an additional identifier and a contact have been provided" - {
        "must return 9 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, qualifier)
            .setValue(AddIdentifierYesNoPage, true)
            .setValue(AddContactYesNoPage, true)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = injector.instanceOf[LocationOfGoodsAnswersViewModelProvider]
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustBe 9
              section.sectionTitle.get mustBe "Location of goods"
          }
        }
      }

      "when neither an additional identifier nor a contact have been provided" - {
        "must return 6 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, qualifier)
            .setValue(AddIdentifierYesNoPage, false)
            .setValue(AddContactYesNoPage, false)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = injector.instanceOf[LocationOfGoodsAnswersViewModelProvider]
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustBe 6
              section.sectionTitle.get mustBe "Location of goods"
          }
        }
      }
    }

    "when 'W' coordinates" - {
      val qualifier = LocationOfGoodsIdentification(CoordinatesIdentifier, "CoordinatesIdentifier")

      "when a contact has been provided" - {
        "must return 7 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, qualifier)
            .setValue(AddContactYesNoPage, true)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = injector.instanceOf[LocationOfGoodsAnswersViewModelProvider]
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustBe 7
              section.sectionTitle.get mustBe "Location of goods"
          }
        }
      }

      "when a contact has not been provided" - {
        "must return 5 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, qualifier)
            .setValue(AddContactYesNoPage, false)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = injector.instanceOf[LocationOfGoodsAnswersViewModelProvider]
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustBe 5
              section.sectionTitle.get mustBe "Location of goods"
          }
        }
      }
    }

    "when 'U' UN-LOCODE" - {
      val qualifier = LocationOfGoodsIdentification(UnlocodeIdentifier, "UnlocodeIdentifier")

      "when a contact has been provided" - {
        "must return 7 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, qualifier)
            .setValue(AddContactYesNoPage, true)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = injector.instanceOf[LocationOfGoodsAnswersViewModelProvider]
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustBe 7
              section.sectionTitle.get mustBe "Location of goods"
          }
        }
      }

      "when a contact has not been provided" - {
        "must return 5 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, qualifier)
            .setValue(AddContactYesNoPage, false)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = injector.instanceOf[LocationOfGoodsAnswersViewModelProvider]
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustBe 5
              section.sectionTitle.get mustBe "Location of goods"
          }
        }
      }
    }

    "when 'Z' address" - {
      val qualifier = LocationOfGoodsIdentification(AddressIdentifier, "AddressIdentifier")

      "when a contact has been provided" - {
        "must return 8 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, qualifier)
            .setValue(AddContactYesNoPage, true)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = injector.instanceOf[LocationOfGoodsAnswersViewModelProvider]
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustBe 8
              section.sectionTitle.get mustBe "Location of goods"
          }
        }
      }

      "when a contact has not been provided" - {
        "must return 6 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, qualifier)
            .setValue(AddContactYesNoPage, false)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = injector.instanceOf[LocationOfGoodsAnswersViewModelProvider]
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustBe 6
              section.sectionTitle.get mustBe "Location of goods"
          }
        }
      }
    }

    "when 'T' postal code" - {
      val qualifier = LocationOfGoodsIdentification(PostalCodeIdentifier, "PostalCode")

      "when a contact has been provided" - {
        "must return 7 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, qualifier)
            .setValue(AddContactYesNoPage, true)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = injector.instanceOf[LocationOfGoodsAnswersViewModelProvider]
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustBe 7
              section.sectionTitle.get mustBe "Location of goods"
          }
        }
      }

      "when a contact has not been provided" - {
        "must return 5 rows" in {
          val initialAnswers = baseAnswers
            .setValue(IdentificationPage, qualifier)
            .setValue(AddContactYesNoPage, false)

          forAll(arbitraryLocationOfGoodsAnswers(initialAnswers), arbitrary[Mode]) {
            (userAnswers, mode) =>
              val viewModelProvider = injector.instanceOf[LocationOfGoodsAnswersViewModelProvider]
              val section           = viewModelProvider.apply(userAnswers, mode).section
              section.rows.size mustBe 5
              section.sectionTitle.get mustBe "Location of goods"
          }
        }
      }
    }
  }
}
