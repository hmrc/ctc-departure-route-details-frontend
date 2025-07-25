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
import controllers.locationOfGoods.{contact, routes}
import generators.Generators
import models.reference.{Country, CustomsOffice, LocationOfGoodsIdentification, LocationType}
import models.{Coordinates, DynamicAddress, Mode}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.locationOfGoods.contact.{NamePage, TelephoneNumberPage}
import pages.locationOfGoods._
import uk.gov.hmrc.govukfrontend.views.Aliases.HtmlContent
import uk.gov.hmrc.govukfrontend.views.html.components.implicits._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.cyaHelpers.locationOfGoods.LocationOfGoodsCheckYourAnswersHelper

class LocationOfGoodsCheckYourAnswersHelperSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "LocationOfGoodsCheckYourAnswersHelper" - {

    "addLocationOfGoods" - {
      "must return None" - {
        "when AddLocationOfGoodsPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LocationOfGoodsCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.addLocationOfGoods
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when AddLocationOfGoodsPage is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddLocationOfGoodsPage, true)
              val helper  = new LocationOfGoodsCheckYourAnswersHelper(answers, mode)
              val result  = helper.addLocationOfGoods.get

              result.key.value mustEqual "Do you want to add a location of goods?"
              result.value.value mustEqual "Yes"
              val actions = result.actions.get.items
              actions.size mustEqual 1
              val action = actions.head
              action.content.value mustEqual "Change"
              action.href mustEqual routes.AddLocationOfGoodsController.onPageLoad(answers.lrn, mode).url
              action.visuallyHiddenText.get mustEqual "if you want to add a location of goods"
              action.id mustEqual "change-add-location-of-goods"
          }
        }
      }
    }

    "locationType" - {
      "must return None" - {
        "when locationTypePage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LocationOfGoodsCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.locationType
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when locationTypePage defined" in {
          forAll(arbitrary[Mode], arbitrary[LocationType]) {
            (mode, locationType) =>
              val answers = emptyUserAnswers.setValue(LocationTypePage, locationType)
              val helper  = new LocationOfGoodsCheckYourAnswersHelper(answers, mode)
              val result  = helper.locationType

              result.value mustEqual
                SummaryListRow(
                  key = Key("Location type".toText),
                  value = Value(messages(s"$locationType").toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.LocationTypeController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("location type"),
                          attributes = Map("id" -> "change-location-type")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "locationOfGoodsIdentification" - {
      "must return None" - {
        "when locationOfGoodsIdentificationPage undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LocationOfGoodsCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.locationOfGoodsIdentification
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when locationOfGoodsIdentificationPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[LocationOfGoodsIdentification]) {
            (mode, locationOfGoodsIdentification) =>
              val answers = emptyUserAnswers.setValue(IdentificationPage, locationOfGoodsIdentification)
              val helper  = new LocationOfGoodsCheckYourAnswersHelper(answers, mode)
              val result  = helper.locationOfGoodsIdentification

              result.value mustEqual
                SummaryListRow(
                  key = Key("Identifier type for the location of goods".toText),
                  value = Value(messages(s"$locationOfGoodsIdentification").toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.IdentificationController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("identifier type for the location of goods"),
                          attributes = Map("id" -> "change-location-of-goods-identification")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "customsOfficeIdentifier" - {
      "must return None" - {
        "when customsOfficeIdentifierPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LocationOfGoodsCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.customsOfficeIdentifier
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when customsOfficeIdentifierPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[CustomsOffice]) {
            (mode, customsOffice) =>
              val answers = emptyUserAnswers.setValue(CustomsOfficeIdentifierPage, customsOffice)
              val helper  = new LocationOfGoodsCheckYourAnswersHelper(answers, mode)
              val result  = helper.customsOfficeIdentifier

              result.value mustEqual
                SummaryListRow(
                  key = Key("What is the customs office identifier for the location of goods?".toText),
                  value = Value(customsOffice.toString.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.CustomsOfficeIdentifierController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("the customs office identifier for the location of goods"),
                          attributes = Map("id" -> "change-location-of-goods-customs-office-identifier")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "eori" - {
      "must return None" - {
        "when eoriPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LocationOfGoodsCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.eori
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when eoriPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, eori) =>
              val answers = emptyUserAnswers.setValue(EoriPage, eori)
              val helper  = new LocationOfGoodsCheckYourAnswersHelper(answers, mode)
              val result  = helper.eori

              result.value mustEqual
                SummaryListRow(
                  key = Key("EORI number or Trader Identification Number (TIN) for the location of goods".toText),
                  value = Value(eori.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.EoriController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("EORI number or Trader Identification Number (TIN) for the location of goods"),
                          attributes = Map("id" -> "change-location-of-goods-eori")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "authorisationNumber" - {
      "must return None" - {
        "when authorisationNumberPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LocationOfGoodsCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.authorisationNumber
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when authorisationNumberPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, authorisationNumber) =>
              val answers = emptyUserAnswers.setValue(AuthorisationNumberPage, authorisationNumber)
              val helper  = new LocationOfGoodsCheckYourAnswersHelper(answers, mode)
              val result  = helper.authorisationNumber

              result.value mustEqual
                SummaryListRow(
                  key = Key("Authorisation number".toText),
                  value = Value(authorisationNumber.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.AuthorisationNumberController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("the authorisation number for the location of goods"),
                          attributes = Map("id" -> "change-location-of-goods-authorisation-number")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "coordinates" - {
      "must return None" - {
        "when coordinatesPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LocationOfGoodsCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.coordinates
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when coordinatesPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[Coordinates]) {
            (mode, coordinates) =>
              val answers = emptyUserAnswers.setValue(CoordinatesPage, coordinates)
              val helper  = new LocationOfGoodsCheckYourAnswersHelper(answers, mode)
              val result  = helper.coordinates

              result.value mustEqual
                SummaryListRow(
                  key = Key("Coordinates".toText),
                  value = Value(coordinates.toString.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.CoordinatesController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("coordinates"),
                          attributes = Map("id" -> "change-location-of-goods-coordinates")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "unLocode" - {
      "must return None" - {
        "when unLocodePage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LocationOfGoodsCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.unLocode
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when unLocodePage is defined" in {
          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, unLocode) =>
              val answers = emptyUserAnswers.setValue(UnLocodePage, unLocode)
              val helper  = new LocationOfGoodsCheckYourAnswersHelper(answers, mode)
              val result  = helper.unLocode

              result.value mustEqual
                SummaryListRow(
                  key = Key("UN/LOCODE for the location of goods".toText),
                  value = Value(unLocode.toString.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.UnLocodeController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("the UN/LOCODE for the location of goods"),
                          attributes = Map("id" -> "change-location-of-goods-un-locode")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "address" - {
      "must return None" - {
        "when addressPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LocationOfGoodsCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.address
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when addressPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[DynamicAddress]) {
            (mode, address) =>
              val answers = emptyUserAnswers.setValue(AddressPage, address)
              val helper  = new LocationOfGoodsCheckYourAnswersHelper(answers, mode)
              val result  = helper.address

              result.value mustEqual
                SummaryListRow(
                  key = Key("What is the address for the location of goods?".toText),
                  value = Value(HtmlContent(address.toString)),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.AddressController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("the address for the location of goods"),
                          attributes = Map("id" -> "change-location-of-goods-address")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "country" - {
      "must return None" - {
        "when countryPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LocationOfGoodsCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.country
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when countryPage is defined" in {
          forAll(arbitrary[Mode], arbitrary[Country]) {
            (mode, country) =>
              val answers = emptyUserAnswers.setValue(CountryPage, country)
              val helper  = new LocationOfGoodsCheckYourAnswersHelper(answers, mode)
              val result  = helper.country

              result.value mustEqual
                SummaryListRow(
                  key = Key("Location of goods country".toText),
                  value = Value(country.toString.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.CountryController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("location of goods country"),
                          attributes = Map("id" -> "change-location-of-goods-country")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "additionalIdentifierYesNo" - {
      "must return None" - {
        "when addIdentifierYesNoPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LocationOfGoodsCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.additionalIdentifierYesNo
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when addIdentifierYesNoPage is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddIdentifierYesNoPage, true)
              val helper  = new LocationOfGoodsCheckYourAnswersHelper(answers, mode)
              val result  = helper.additionalIdentifierYesNo

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add another identifier for the location of goods?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.AddIdentifierYesNoController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you want to add another identifier for the location of goods"),
                          attributes = Map("id" -> "change-location-of-goods-add-identifier")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "additionalIdentifier" - {
      "must return None" - {
        "when additionalIdentifierPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LocationOfGoodsCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.additionalIdentifier
              result must not be defined
          }
        }
      }
    }

    "contactYesNo" - {
      "must return None" - {
        "when contactYesNoPage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LocationOfGoodsCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.contactYesNo
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when addContactYesNoPage is defined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val answers = emptyUserAnswers.setValue(AddContactYesNoPage, true)
              val helper  = new LocationOfGoodsCheckYourAnswersHelper(answers, mode)
              val result  = helper.contactYesNo

              result.value mustEqual
                SummaryListRow(
                  key = Key("Do you want to add a contact for the location of goods?".toText),
                  value = Value("Yes".toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = routes.AddContactYesNoController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("if you want to add a contact for the location of goods"),
                          attributes = Map("id" -> "change-location-of-goods-add-contact")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "contactName" - {
      "must return None" - {
        "when contactNamePage is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LocationOfGoodsCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.contactName
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when contactNamePage is defined" in {
          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, contactName) =>
              val answers = emptyUserAnswers.setValue(NamePage, contactName)
              val helper  = new LocationOfGoodsCheckYourAnswersHelper(answers, mode)
              val result  = helper.contactName

              result.value mustEqual
                SummaryListRow(
                  key = Key("Contact’s name".toText),
                  value = Value(contactName.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = contact.routes.NameController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("contact’s name for the location of goods"),
                          attributes = Map("id" -> "change-location-of-goods-contact")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

    "contactPhoneNumber" - {
      "must return None" - {
        "when contactPhoneNumber is undefined" in {
          forAll(arbitrary[Mode]) {
            mode =>
              val helper = new LocationOfGoodsCheckYourAnswersHelper(emptyUserAnswers, mode)
              val result = helper.contactPhoneNumber
              result must not be defined
          }
        }
      }

      "must return Some(Row)" - {
        "when contactPhoneNumber is defined" in {
          forAll(arbitrary[Mode], arbitrary[String]) {
            (mode, contactPhoneNumber) =>
              val answers = emptyUserAnswers.setValue(TelephoneNumberPage, contactPhoneNumber)
              val helper  = new LocationOfGoodsCheckYourAnswersHelper(answers, mode)
              val result  = helper.contactPhoneNumber

              result.value mustEqual
                SummaryListRow(
                  key = Key("Contact’s phone number".toText),
                  value = Value(contactPhoneNumber.toText),
                  actions = Some(
                    Actions(
                      items = List(
                        ActionItem(
                          content = "Change".toText,
                          href = contact.routes.TelephoneNumberController.onPageLoad(answers.lrn, mode).url,
                          visuallyHiddenText = Some("contact’s phone number for the location of goods"),
                          attributes = Map("id" -> "change-location-of-goods-contact-phone-number")
                        )
                      )
                    )
                  )
                )
          }
        }
      }
    }

  }
}
