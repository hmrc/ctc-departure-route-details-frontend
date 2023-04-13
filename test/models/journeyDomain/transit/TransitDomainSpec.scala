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

package models.journeyDomain.transit

import base.SpecBase
import generators.Generators
import models.DeclarationType._
import models.Index
import models.SecurityDetailsType.NoSecurityDetails
import models.domain.{EitherType, UserAnswersReader}
import models.reference.{Country, CustomsOffice}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import pages.external.{DeclarationTypePage, OfficeOfDepartureInCL112Page, OfficeOfDeparturePage, SecurityDetailsTypePage}
import pages.routing._
import pages.routing.index.{CountryOfRoutingInCL112Page, CountryOfRoutingPage}
import pages.transit._
import pages.transit.index._

class TransitDomainSpec extends SpecBase with Generators {

  "TransitDomain" - {

    val country         = arbitrary[Country].sample.value
    val customsOffice   = arbitrary[CustomsOffice].sample.value
    val officeOfTransit = arbitrary[CustomsOffice].sample.value

    "can be parsed from UserAnswers" - {

      "when offices of departure and destination country codes are in set CL112 and both have same country code" in {
        val userAnswers = emptyUserAnswers
          .setValue(OfficeOfDeparturePage, customsOffice)
          .setValue(OfficeOfDepartureInCL112Page, true)
          .setValue(OfficeOfDestinationPage, customsOffice)
          .setValue(OfficeOfDestinationInCL112Page, true)
          .setValue(AddOfficeOfTransitYesNoPage, false)

        val expectedResult = TransitDomain(
          isT2DeclarationType = None,
          officesOfTransit = Nil
        )

        val result: EitherType[TransitDomain] = UserAnswersReader[TransitDomain].run(userAnswers)

        result.value mustBe expectedResult
      }

      "when T2 declaration type" in {
        val userAnswers = emptyUserAnswers
          .setValue(OfficeOfDeparturePage, customsOffice)
          .setValue(OfficeOfDepartureInCL112Page, false)
          .setValue(DeclarationTypePage, Option2)
          .setValue(SecurityDetailsTypePage, NoSecurityDetails)
          .setValue(OfficeOfDestinationPage, customsOffice)
          .setValue(OfficeOfDestinationInCL112Page, false)
          .setValue(OfficeOfTransitCountryPage(index), country)
          .setValue(OfficeOfTransitPage(index), officeOfTransit)
          .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

        val expectedResult = TransitDomain(
          isT2DeclarationType = None,
          officesOfTransit = Seq(OfficeOfTransitDomain(Some(country), officeOfTransit, None)(index))
        )

        val result: EitherType[TransitDomain] = UserAnswersReader[TransitDomain].run(userAnswers)

        result.value mustBe expectedResult
      }

      "when T declaration type" - {

        "and some items are T2 declaration type" in {
          val userAnswers = emptyUserAnswers
            .setValue(OfficeOfDeparturePage, customsOffice)
            .setValue(OfficeOfDepartureInCL112Page, false)
            .setValue(DeclarationTypePage, Option5)
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(OfficeOfDestinationPage, customsOffice)
            .setValue(OfficeOfDestinationInCL112Page, false)
            .setValue(T2DeclarationTypeYesNoPage, true)
            .setValue(OfficeOfTransitCountryPage(index), country)
            .setValue(OfficeOfTransitPage(index), officeOfTransit)
            .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

          val expectedResult = TransitDomain(
            isT2DeclarationType = Some(true),
            officesOfTransit = Seq(OfficeOfTransitDomain(Some(country), officeOfTransit, None)(index))
          )

          val result: EitherType[TransitDomain] = UserAnswersReader[TransitDomain].run(userAnswers)

          result.value mustBe expectedResult
        }

        "and no items are T2 declaration type" - {
          "and country code for office of departure is in set CL112" in {
            val userAnswers = emptyUserAnswers
              .setValue(OfficeOfDeparturePage, customsOffice)
              .setValue(OfficeOfDepartureInCL112Page, true)
              .setValue(DeclarationTypePage, Option5)
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(OfficeOfDestinationPage, customsOffice)
              .setValue(OfficeOfDestinationInCL112Page, false)
              .setValue(T2DeclarationTypeYesNoPage, false)
              .setValue(OfficeOfTransitCountryPage(index), country)
              .setValue(OfficeOfTransitPage(index), officeOfTransit)
              .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

            val expectedResult = TransitDomain(
              isT2DeclarationType = Some(false),
              officesOfTransit = Seq(OfficeOfTransitDomain(Some(country), officeOfTransit, None)(index))
            )

            val result: EitherType[TransitDomain] = UserAnswersReader[TransitDomain].run(userAnswers)

            result.value mustBe expectedResult
          }

          "and country code for office of destination is in set CL112" in {
            val userAnswers = emptyUserAnswers
              .setValue(OfficeOfDeparturePage, customsOffice)
              .setValue(OfficeOfDepartureInCL112Page, false)
              .setValue(DeclarationTypePage, Option5)
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(OfficeOfDestinationPage, customsOffice)
              .setValue(OfficeOfDestinationInCL112Page, true)
              .setValue(T2DeclarationTypeYesNoPage, false)
              .setValue(OfficeOfTransitPage(index), officeOfTransit)
              .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

            val expectedResult = TransitDomain(
              isT2DeclarationType = Some(false),
              officesOfTransit = Seq(OfficeOfTransitDomain(None, officeOfTransit, None)(index))
            )

            val result: EitherType[TransitDomain] = UserAnswersReader[TransitDomain].run(userAnswers)

            result.value mustBe expectedResult
          }

          "and country code for neither office of departure nor office of destination is in set CL112" - {

            "and at least one country of routing is in set CL112" in {
              val userAnswers = emptyUserAnswers
                .setValue(OfficeOfDeparturePage, customsOffice)
                .setValue(OfficeOfDepartureInCL112Page, false)
                .setValue(DeclarationTypePage, Option5)
                .setValue(SecurityDetailsTypePage, NoSecurityDetails)
                .setValue(OfficeOfDestinationPage, customsOffice)
                .setValue(OfficeOfDestinationInCL112Page, false)
                .setValue(BindingItineraryPage, true)
                .setValue(CountryOfRoutingPage(index), country)
                .setValue(CountryOfRoutingInCL112Page(index), true)
                .setValue(T2DeclarationTypeYesNoPage, false)
                .setValue(OfficeOfTransitCountryPage(index), country)
                .setValue(OfficeOfTransitPage(index), officeOfTransit)
                .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

              val expectedResult = TransitDomain(
                isT2DeclarationType = Some(false),
                officesOfTransit = Seq(OfficeOfTransitDomain(Some(country), officeOfTransit, None)(index))
              )

              val result: EitherType[TransitDomain] = UserAnswersReader[TransitDomain].run(userAnswers)

              result.value mustBe expectedResult
            }

            "and no countries of routing are in set CL112" in {
              val userAnswers = emptyUserAnswers
                .setValue(OfficeOfDeparturePage, customsOffice)
                .setValue(OfficeOfDepartureInCL112Page, false)
                .setValue(DeclarationTypePage, Option5)
                .setValue(SecurityDetailsTypePage, NoSecurityDetails)
                .setValue(OfficeOfDestinationPage, customsOffice)
                .setValue(OfficeOfDestinationInCL112Page, false)
                .setValue(BindingItineraryPage, true)
                .setValue(CountryOfRoutingPage(index), country)
                .setValue(CountryOfRoutingInCL112Page(index), false)
                .setValue(T2DeclarationTypeYesNoPage, false)
                .setValue(AddOfficeOfTransitYesNoPage, false)

              val expectedResult = TransitDomain(
                isT2DeclarationType = Some(false),
                officesOfTransit = Nil
              )

              val result: EitherType[TransitDomain] = UserAnswersReader[TransitDomain].run(userAnswers)

              result.value mustBe expectedResult
            }
          }
        }
      }

      "when declaration type is neither T nor T2" - {

        val declarationType = Gen.oneOf(Option1, Option3).sample.value

        "and country code for office of departure is in set CL112" in {
          val userAnswers = emptyUserAnswers
            .setValue(OfficeOfDeparturePage, customsOffice)
            .setValue(OfficeOfDepartureInCL112Page, true)
            .setValue(DeclarationTypePage, declarationType)
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(OfficeOfDestinationPage, customsOffice)
            .setValue(OfficeOfDestinationInCL112Page, false)
            .setValue(OfficeOfTransitCountryPage(index), country)
            .setValue(OfficeOfTransitPage(index), officeOfTransit)
            .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

          val expectedResult = TransitDomain(
            isT2DeclarationType = None,
            officesOfTransit = Seq(OfficeOfTransitDomain(Some(country), officeOfTransit, None)(index))
          )

          val result: EitherType[TransitDomain] = UserAnswersReader[TransitDomain].run(userAnswers)

          result.value mustBe expectedResult
        }

        "and country code for office of destination is in set CL112" in {
          val userAnswers = emptyUserAnswers
            .setValue(OfficeOfDeparturePage, customsOffice)
            .setValue(OfficeOfDepartureInCL112Page, false)
            .setValue(DeclarationTypePage, declarationType)
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(OfficeOfDestinationPage, customsOffice)
            .setValue(OfficeOfDestinationInCL112Page, true)
            .setValue(OfficeOfTransitPage(index), officeOfTransit)
            .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

          val expectedResult = TransitDomain(
            isT2DeclarationType = None,
            officesOfTransit = Seq(OfficeOfTransitDomain(None, officeOfTransit, None)(index))
          )

          val result: EitherType[TransitDomain] = UserAnswersReader[TransitDomain].run(userAnswers)

          result.value mustBe expectedResult
        }

        "and country code for neither office of departure nor office of destination is in set CL112" - {

          "and at least one country of routing is in set CL112" in {
            val userAnswers = emptyUserAnswers
              .setValue(OfficeOfDeparturePage, customsOffice)
              .setValue(OfficeOfDepartureInCL112Page, false)
              .setValue(DeclarationTypePage, declarationType)
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(OfficeOfDestinationPage, customsOffice)
              .setValue(OfficeOfDestinationInCL112Page, false)
              .setValue(BindingItineraryPage, true)
              .setValue(CountryOfRoutingPage(index), country)
              .setValue(CountryOfRoutingInCL112Page(index), true)
              .setValue(OfficeOfTransitCountryPage(index), country)
              .setValue(OfficeOfTransitPage(index), officeOfTransit)
              .setValue(AddOfficeOfTransitETAYesNoPage(index), false)

            val expectedResult = TransitDomain(
              isT2DeclarationType = None,
              officesOfTransit = Seq(OfficeOfTransitDomain(Some(country), officeOfTransit, None)(index))
            )

            val result: EitherType[TransitDomain] = UserAnswersReader[TransitDomain].run(userAnswers)

            result.value mustBe expectedResult
          }

          "and no countries of routing are in set CL112" in {
            val userAnswers = emptyUserAnswers
              .setValue(OfficeOfDeparturePage, customsOffice)
              .setValue(OfficeOfDepartureInCL112Page, false)
              .setValue(DeclarationTypePage, declarationType)
              .setValue(SecurityDetailsTypePage, NoSecurityDetails)
              .setValue(OfficeOfDestinationPage, customsOffice)
              .setValue(OfficeOfDestinationInCL112Page, false)
              .setValue(BindingItineraryPage, true)
              .setValue(CountryOfRoutingPage(index), country)
              .setValue(CountryOfRoutingInCL112Page(index), false)
              .setValue(AddOfficeOfTransitYesNoPage, false)

            val expectedResult = TransitDomain(
              isT2DeclarationType = None,
              officesOfTransit = Nil
            )

            val result: EitherType[TransitDomain] = UserAnswersReader[TransitDomain].run(userAnswers)

            result.value mustBe expectedResult
          }
        }
      }
    }

    "cannot be parsed from UserAnswers" - {

      "when offices of departure and destination are in CL112 and have same country code" - {
        "and add office of transit yes/no unanswered" in {
          val userAnswers = emptyUserAnswers
            .setValue(OfficeOfDeparturePage, customsOffice)
            .setValue(OfficeOfDepartureInCL112Page, true)
            .setValue(OfficeOfDestinationPage, customsOffice)
            .setValue(OfficeOfDestinationInCL112Page, true)

          val result: EitherType[TransitDomain] = UserAnswersReader[TransitDomain].run(userAnswers)

          result.left.value.page mustBe AddOfficeOfTransitYesNoPage
        }
      }

      "when declaration type is T2" - {
        "and office of destination in set CL112" - {
          "and empty json at index 0" in {
            val userAnswers = emptyUserAnswers
              .setValue(OfficeOfDeparturePage, customsOffice)
              .setValue(OfficeOfDepartureInCL112Page, false)
              .setValue(DeclarationTypePage, Option2)
              .setValue(OfficeOfDestinationPage, customsOffice)
              .setValue(OfficeOfDestinationInCL112Page, true)

            val result: EitherType[TransitDomain] = UserAnswersReader[TransitDomain].run(userAnswers)

            result.left.value.page mustBe OfficeOfTransitPage(Index(0))
          }
        }

        "and office of destination not in set CL112" - {
          "and empty json at index 0" in {
            val userAnswers = emptyUserAnswers
              .setValue(OfficeOfDeparturePage, customsOffice)
              .setValue(OfficeOfDepartureInCL112Page, false)
              .setValue(DeclarationTypePage, Option2)
              .setValue(OfficeOfDestinationPage, customsOffice)
              .setValue(OfficeOfDestinationInCL112Page, false)

            val result: EitherType[TransitDomain] = UserAnswersReader[TransitDomain].run(userAnswers)

            result.left.value.page mustBe OfficeOfTransitCountryPage(Index(0))
          }
        }
      }

      "when declaration type is neither T nor T2" - {

        val declarationType = Gen.oneOf(Option1, Option3).sample.value

        "and country code for neither office of departure nor office of destination is in set CL112" - {
          "and no countries of routing are in set CL112" - {
            "and add office of transit yes/no unanswered" in {
              val userAnswers = emptyUserAnswers
                .setValue(OfficeOfDeparturePage, customsOffice)
                .setValue(OfficeOfDepartureInCL112Page, false)
                .setValue(DeclarationTypePage, declarationType)
                .setValue(SecurityDetailsTypePage, NoSecurityDetails)
                .setValue(OfficeOfDestinationPage, customsOffice)
                .setValue(OfficeOfDestinationInCL112Page, false)
                .setValue(BindingItineraryPage, true)
                .setValue(CountryOfRoutingPage(index), country)
                .setValue(CountryOfRoutingInCL112Page(index), false)

              val result: EitherType[TransitDomain] = UserAnswersReader[TransitDomain].run(userAnswers)

              result.left.value.page mustBe AddOfficeOfTransitYesNoPage
            }
          }
        }
      }
    }
  }
}
