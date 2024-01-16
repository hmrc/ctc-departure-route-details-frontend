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

package models.journeyDomain.locationOfGoods

import base.SpecBase
import config.Constants.LocationOfGoodsIdentifier._
import generators.Generators
import models.domain.UserAnswersReader
import models.reference.{Country, CustomsOffice}
import models.{Coordinates, DynamicAddress, LocationOfGoodsIdentification, LocationType, PostalCodeAddress}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks.forAll
import pages.QuestionPage
import pages.locationOfGoods._

class LocationOfGoodsDomainSpec extends SpecBase with Generators {

  private val typeOfLocation = arbitrary[LocationType].sample.value
  private val contactName    = Gen.alphaNumStr.sample.value
  private val contactPhone   = Gen.alphaNumStr.sample.value

  "LocationOfGoodsDomain" - {

    "can be parsed from UserAnswers" - {

      "when qualifier of identification" - {

        "is V (Customs office identifier)" in {
          val qualifierOfIdentification = LocationOfGoodsIdentification(CustomsOfficeIdentifier, "CustomsOfficeIdentifier")
          val customsOffice             = arbitrary[CustomsOffice].sample.value

          val userAnswers = emptyUserAnswers
            .setValue(LocationTypePage, typeOfLocation)
            .setValue(IdentificationPage, qualifierOfIdentification)
            .setValue(CustomsOfficeIdentifierPage, customsOffice)

          val expectedResult = LocationOfGoodsV(
            typeOfLocation = typeOfLocation,
            customsOffice = customsOffice
          )

          val result = UserAnswersReader[LocationOfGoodsDomain](
            LocationOfGoodsDomain.userAnswersReader.apply(Nil)
          ).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            LocationTypePage,
            IdentificationPage,
            CustomsOfficeIdentifierPage
          )
        }

        "is W (Coordinate identifier)" in {
          val qualifierOfIdentification = LocationOfGoodsIdentification(CoordinatesIdentifier, "CoordinatesIdentifier")
          val coordinate                = arbitrary[Coordinates].sample.value

          val userAnswers = emptyUserAnswers
            .setValue(LocationTypePage, typeOfLocation)
            .setValue(IdentificationPage, qualifierOfIdentification)
            .setValue(CoordinatesPage, coordinate)
            .setValue(AddContactYesNoPage, false)

          val expectedResult = LocationOfGoodsW(
            typeOfLocation = typeOfLocation,
            coordinates = coordinate,
            additionalContact = None
          )

          val result = UserAnswersReader[LocationOfGoodsDomain](
            LocationOfGoodsDomain.userAnswersReader.apply(Nil)
          ).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            LocationTypePage,
            IdentificationPage,
            CoordinatesPage,
            AddContactYesNoPage
          )
        }

        "is X (EORI number) and AddIdentifierYesNoPage is answered No" in {
          val qualifierOfIdentification = LocationOfGoodsIdentification(EoriNumberIdentifier, "EoriNumber")
          val eoriNumber                = Gen.alphaNumStr.sample.value

          val userAnswers = emptyUserAnswers
            .setValue(LocationTypePage, typeOfLocation)
            .setValue(IdentificationPage, qualifierOfIdentification)
            .setValue(EoriPage, eoriNumber)
            .setValue(AddIdentifierYesNoPage, false)
            .setValue(AddContactYesNoPage, false)

          val expectedResult = LocationOfGoodsX(
            typeOfLocation = typeOfLocation,
            identificationNumber = eoriNumber,
            additionalIdentifier = None,
            additionalContact = None
          )

          val result = UserAnswersReader[LocationOfGoodsDomain](
            LocationOfGoodsDomain.userAnswersReader.apply(Nil)
          ).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            LocationTypePage,
            IdentificationPage,
            EoriPage,
            AddIdentifierYesNoPage,
            AddContactYesNoPage
          )
        }

        "is X (EORI number) and AddIdentifierYesNoPage is answered Yes" in {
          val qualifierOfIdentification = LocationOfGoodsIdentification(EoriNumberIdentifier, "EoriNumber")
          val eoriNumber                = Gen.alphaNumStr.sample.value

          val userAnswers = emptyUserAnswers
            .setValue(LocationTypePage, typeOfLocation)
            .setValue(IdentificationPage, qualifierOfIdentification)
            .setValue(EoriPage, eoriNumber)
            .setValue(AddIdentifierYesNoPage, true)
            .setValue(AdditionalIdentifierPage, "1234")
            .setValue(AddContactYesNoPage, false)

          val expectedResult = LocationOfGoodsX(
            typeOfLocation = typeOfLocation,
            identificationNumber = eoriNumber,
            additionalIdentifier = Some("1234"),
            additionalContact = None
          )

          val result = UserAnswersReader[LocationOfGoodsDomain](
            LocationOfGoodsDomain.userAnswersReader.apply(Nil)
          ).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            LocationTypePage,
            IdentificationPage,
            EoriPage,
            AddIdentifierYesNoPage,
            AdditionalIdentifierPage,
            AddContactYesNoPage
          )
        }

        "is Y (Authorisation number) and AddIdentifierYesNoPage is answered No" in {
          val qualifierOfIdentification = LocationOfGoodsIdentification(AuthorisationNumberIdentifier, "AuthorisationNumberIdentifier")
          val authorisationNumber       = Gen.alphaNumStr.sample.value

          val userAnswers = emptyUserAnswers
            .setValue(InferredLocationTypePage, typeOfLocation)
            .setValue(InferredIdentificationPage, qualifierOfIdentification)
            .setValue(AuthorisationNumberPage, authorisationNumber)
            .setValue(AddIdentifierYesNoPage, false)
            .setValue(AddContactYesNoPage, false)

          val expectedResult = LocationOfGoodsY(
            typeOfLocation = typeOfLocation,
            authorisationNumber = authorisationNumber,
            additionalIdentifier = None,
            additionalContact = None
          )

          val result = UserAnswersReader[LocationOfGoodsDomain](
            LocationOfGoodsDomain.userAnswersReader.apply(Nil)
          ).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            AuthorisationNumberPage,
            AddIdentifierYesNoPage,
            AddContactYesNoPage
          )
        }

        "is Y (Authorisation number) and AddIdentifierYesNoPage is answered Yes" in {
          val qualifierOfIdentification = LocationOfGoodsIdentification(AuthorisationNumberIdentifier, "AuthorisationNumberIdentifier")
          val authorisationNumber       = Gen.alphaNumStr.sample.value

          val userAnswers = emptyUserAnswers
            .setValue(LocationTypePage, typeOfLocation)
            .setValue(InferredIdentificationPage, qualifierOfIdentification)
            .setValue(AuthorisationNumberPage, authorisationNumber)
            .setValue(AddIdentifierYesNoPage, true)
            .setValue(AdditionalIdentifierPage, "1234")
            .setValue(AddContactYesNoPage, false)

          val expectedResult = LocationOfGoodsY(
            typeOfLocation = typeOfLocation,
            authorisationNumber = authorisationNumber,
            additionalIdentifier = Some("1234"),
            additionalContact = None
          )

          val result = UserAnswersReader[LocationOfGoodsDomain](
            LocationOfGoodsDomain.userAnswersReader.apply(Nil)
          ).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            LocationTypePage,
            AuthorisationNumberPage,
            AddIdentifierYesNoPage,
            AdditionalIdentifierPage,
            AddContactYesNoPage
          )
        }

        "is Z (Address)" in {
          val qualifierOfIdentification = LocationOfGoodsIdentification(AddressIdentifier, "AddressIdentifier")
          val country                   = arbitrary[Country].sample.value
          val address                   = arbitrary[DynamicAddress].sample.value

          val userAnswers = emptyUserAnswers
            .setValue(LocationTypePage, typeOfLocation)
            .setValue(IdentificationPage, qualifierOfIdentification)
            .setValue(CountryPage, country)
            .setValue(AddressPage, address)
            .setValue(AddContactYesNoPage, false)

          val expectedResult = LocationOfGoodsZ(
            typeOfLocation = typeOfLocation,
            country = country,
            address = address,
            additionalContact = None
          )

          val result = UserAnswersReader[LocationOfGoodsDomain](
            LocationOfGoodsDomain.userAnswersReader.apply(Nil)
          ).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            LocationTypePage,
            IdentificationPage,
            CountryPage,
            AddressPage,
            AddContactYesNoPage
          )
        }

        "is U (UnLocode)" in {
          val qualifierOfIdentification = LocationOfGoodsIdentification(UnlocodeIdentifier, "UnlocodeIdentifier")
          val unLocode                  = arbitrary[String].sample.value

          val userAnswers = emptyUserAnswers
            .setValue(LocationTypePage, typeOfLocation)
            .setValue(IdentificationPage, qualifierOfIdentification)
            .setValue(UnLocodePage, unLocode)
            .setValue(AddContactYesNoPage, false)

          val expectedResult = LocationOfGoodsU(
            typeOfLocation = typeOfLocation,
            unLocode = unLocode,
            additionalContact = None
          )

          val result = UserAnswersReader[LocationOfGoodsDomain](
            LocationOfGoodsDomain.userAnswersReader.apply(Nil)
          ).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            LocationTypePage,
            IdentificationPage,
            UnLocodePage,
            AddContactYesNoPage
          )
        }

        "is T (PostalCode)" in {
          val qualifierOfIdentification = LocationOfGoodsIdentification(PostalCodeIdentifier, "PostalCode")
          val postalCodeAddress         = arbitrary[PostalCodeAddress].sample.value

          val userAnswers = emptyUserAnswers
            .setValue(LocationTypePage, typeOfLocation)
            .setValue(IdentificationPage, qualifierOfIdentification)
            .setValue(PostalCodePage, postalCodeAddress)
            .setValue(AddContactYesNoPage, false)

          val expectedResult = LocationOfGoodsT(
            typeOfLocation = typeOfLocation,
            postalCodeAddress = postalCodeAddress,
            additionalContact = None
          )

          val result = UserAnswersReader[LocationOfGoodsDomain](
            LocationOfGoodsDomain.userAnswersReader.apply(Nil)
          ).run(userAnswers)

          result.value.value mustBe expectedResult
          result.value.pages mustBe Seq(
            LocationTypePage,
            IdentificationPage,
            PostalCodePage,
            AddContactYesNoPage
          )
        }

      }

      "when is Y(Authorisation Number) and all optional pages are answered" in {
        val qualifierOfIdentification = LocationOfGoodsIdentification(AuthorisationNumberIdentifier, "AuthorisationNumberIdentifier")
        val authorisationNumber       = Gen.alphaNumStr.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(LocationTypePage, typeOfLocation)
          .setValue(InferredIdentificationPage, qualifierOfIdentification)
          .setValue(AuthorisationNumberPage, authorisationNumber)
          .setValue(AddIdentifierYesNoPage, true)
          .setValue(AdditionalIdentifierPage, "1234")
          .setValue(AddContactYesNoPage, true)
          .setValue(contact.NamePage, contactName)
          .setValue(contact.TelephoneNumberPage, contactPhone)

        val result = UserAnswersReader[LocationOfGoodsDomain](
          LocationOfGoodsDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        val expectedResult = LocationOfGoodsY(
          typeOfLocation = typeOfLocation,
          authorisationNumber = authorisationNumber,
          additionalIdentifier = Some("1234"),
          additionalContact = Some(AdditionalContactDomain(contactName, contactPhone))
        )

        result.value.value mustBe expectedResult
        result.value.pages mustBe Seq(
          LocationTypePage,
          AuthorisationNumberPage,
          AddIdentifierYesNoPage,
          AdditionalIdentifierPage,
          AddContactYesNoPage,
          contact.NamePage,
          contact.TelephoneNumberPage
        )
      }

    }

    "cannot be parsed from UserAnswers" - {

      "when is X(Eori Number) and a mandatory page is missing" in {
        val mandatoryPages: Gen[QuestionPage[_]] = Gen.oneOf(
          IdentificationPage,
          EoriPage,
          AddIdentifierYesNoPage,
          AddContactYesNoPage
        )

        val qualifierOfIdentification = LocationOfGoodsIdentification(EoriNumberIdentifier, "EoriNumber")
        val eoriNumber                = Gen.alphaNumStr.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(LocationTypePage, typeOfLocation)
          .setValue(IdentificationPage, qualifierOfIdentification)
          .setValue(EoriPage, eoriNumber)
          .setValue(AddIdentifierYesNoPage, false)
          .setValue(AddContactYesNoPage, false)

        forAll(mandatoryPages) {
          mandatoryPage =>
            val invalidUserAnswers = userAnswers.removeValue(mandatoryPage)

            val result = UserAnswersReader[LocationOfGoodsDomain](
              LocationOfGoodsDomain.userAnswersReader.apply(Nil)
            ).run(invalidUserAnswers)

            result.left.value.page mustBe mandatoryPage
        }
      }

      "when is Y(Authorisation Number) and AdditionalIdentifier page is missing when required" in {
        val qualifierOfIdentification = LocationOfGoodsIdentification(AuthorisationNumberIdentifier, "AuthorisationNumberIdentifier")
        val authorisationNumber       = Gen.alphaNumStr.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(LocationTypePage, typeOfLocation)
          .setValue(InferredIdentificationPage, qualifierOfIdentification)
          .setValue(AuthorisationNumberPage, authorisationNumber)
          .setValue(AddIdentifierYesNoPage, true)
          .setValue(AddContactYesNoPage, false)

        val result = UserAnswersReader[LocationOfGoodsDomain](
          LocationOfGoodsDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustBe AdditionalIdentifierPage
        result.left.value.pages mustBe Seq(
          LocationTypePage,
          AuthorisationNumberPage,
          AddIdentifierYesNoPage,
          AdditionalIdentifierPage
        )
      }

      "when is Y(Authorisation Number) and contact name details is missing when required" in {
        val qualifierOfIdentification = LocationOfGoodsIdentification(AuthorisationNumberIdentifier, "AuthorisationNumberIdentifier")
        val authorisationNumber       = Gen.alphaNumStr.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(LocationTypePage, typeOfLocation)
          .setValue(InferredIdentificationPage, qualifierOfIdentification)
          .setValue(AuthorisationNumberPage, authorisationNumber)
          .setValue(AddIdentifierYesNoPage, true)
          .setValue(AdditionalIdentifierPage, "1234")
          .setValue(AddContactYesNoPage, true)

        val result = UserAnswersReader[LocationOfGoodsDomain](
          LocationOfGoodsDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustBe contact.NamePage
        result.left.value.pages mustBe Seq(
          LocationTypePage,
          AuthorisationNumberPage,
          AddIdentifierYesNoPage,
          AdditionalIdentifierPage,
          AddContactYesNoPage,
          contact.NamePage
        )
      }

      "when is Y(Authorisation Number) and contact phone number details is missing when required" in {
        val qualifierOfIdentification = LocationOfGoodsIdentification(AuthorisationNumberIdentifier, "AuthorisationNumberIdentifier")
        val authorisationNumber       = Gen.alphaNumStr.sample.value

        val userAnswers = emptyUserAnswers
          .setValue(LocationTypePage, typeOfLocation)
          .setValue(InferredIdentificationPage, qualifierOfIdentification)
          .setValue(AuthorisationNumberPage, authorisationNumber)
          .setValue(AddIdentifierYesNoPage, true)
          .setValue(AdditionalIdentifierPage, "1234")
          .setValue(AddContactYesNoPage, true)
          .setValue(contact.NamePage, contactName)

        val result = UserAnswersReader[LocationOfGoodsDomain](
          LocationOfGoodsDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustBe contact.TelephoneNumberPage
        result.left.value.pages mustBe Seq(
          LocationTypePage,
          AuthorisationNumberPage,
          AddIdentifierYesNoPage,
          AdditionalIdentifierPage,
          AddContactYesNoPage,
          contact.NamePage,
          contact.TelephoneNumberPage
        )
      }

      "when type is Z (address) and country page is missing" in {
        val qualifierOfIdentification = LocationOfGoodsIdentification(AddressIdentifier, "AddressIdentifier")

        val userAnswers = emptyUserAnswers
          .setValue(LocationTypePage, typeOfLocation)
          .setValue(IdentificationPage, qualifierOfIdentification)

        val result = UserAnswersReader[LocationOfGoodsDomain](
          LocationOfGoodsDomain.userAnswersReader.apply(Nil)
        ).run(userAnswers)

        result.left.value.page mustBe CountryPage
        result.left.value.pages mustBe Seq(
          LocationTypePage,
          IdentificationPage,
          CountryPage
        )
      }
    }
  }

}
