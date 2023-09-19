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

package models.journeyDomain.routing

import base.SpecBase
import config.Constants._
import config.PhaseConfig
import generators.Generators
import models.domain.{EitherType, UserAnswersReader}
import models.reference.{Country, CustomsOffice}
import models.{Index, Phase}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import pages.external.SecurityDetailsTypePage
import pages.routing._
import pages.routing.index.CountryOfRoutingPage

class RoutingDomainSpec extends SpecBase with Generators {

  "RoutingDomain" - {

    val officeOfDestination = arbitrary[CustomsOffice].sample.value
    val country             = arbitrary[Country].sample.value

    "can be parsed from UserAnswers" - {

      "when post transition" - {
        val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
        when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

        "when no security" - {

          val securityType = NoSecurityDetails

          "and following binding itinerary" in {

            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, securityType)
              .setValue(CountryOfDestinationPage, country)
              .setValue(OfficeOfDestinationPage, officeOfDestination)
              .setValue(BindingItineraryPage, true)
              .setValue(CountryOfRoutingPage(index), country)

            val expectedResult = RoutingDomain(
              countryOfDestination = country,
              officeOfDestination = officeOfDestination,
              bindingItinerary = true,
              countriesOfRouting = Seq(
                CountryOfRoutingDomain(country)(index)
              )
            )

            val result: EitherType[RoutingDomain] = UserAnswersReader[RoutingDomain](RoutingDomain.userAnswersReader(mockPhaseConfig)).run(userAnswers)

            result.value mustBe expectedResult
          }

          "and not following binding itinerary" in {

            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, securityType)
              .setValue(CountryOfDestinationPage, country)
              .setValue(OfficeOfDestinationPage, officeOfDestination)
              .setValue(BindingItineraryPage, false)
              .setValue(AddCountryOfRoutingYesNoPage, false)

            val expectedResult = RoutingDomain(
              countryOfDestination = country,
              officeOfDestination = officeOfDestination,
              bindingItinerary = false,
              countriesOfRouting = Nil
            )

            val result: EitherType[RoutingDomain] = UserAnswersReader[RoutingDomain](RoutingDomain.userAnswersReader(mockPhaseConfig)).run(userAnswers)

            result.value mustBe expectedResult
          }
        }

        "when there is security" - {

          val securityType = arbitrary[String](arbitrarySomeSecurityDetailsType).sample.value

          "and following binding itinerary" in {

            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, securityType)
              .setValue(CountryOfDestinationPage, country)
              .setValue(OfficeOfDestinationPage, officeOfDestination)
              .setValue(BindingItineraryPage, true)
              .setValue(CountryOfRoutingPage(index), country)

            val expectedResult = RoutingDomain(
              countryOfDestination = country,
              officeOfDestination = officeOfDestination,
              bindingItinerary = true,
              countriesOfRouting = Seq(
                CountryOfRoutingDomain(country)(index)
              )
            )

            val result: EitherType[RoutingDomain] = UserAnswersReader[RoutingDomain](RoutingDomain.userAnswersReader(mockPhaseConfig)).run(userAnswers)

            result.value mustBe expectedResult
          }

          "and not following binding itinerary" in {

            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, securityType)
              .setValue(CountryOfDestinationPage, country)
              .setValue(OfficeOfDestinationPage, officeOfDestination)
              .setValue(BindingItineraryPage, false)
              .setValue(CountryOfRoutingPage(index), country)

            val expectedResult = RoutingDomain(
              countryOfDestination = country,
              officeOfDestination = officeOfDestination,
              bindingItinerary = false,
              countriesOfRouting = Seq(
                CountryOfRoutingDomain(country)(index)
              )
            )

            val result: EitherType[RoutingDomain] = UserAnswersReader[RoutingDomain](RoutingDomain.userAnswersReader(mockPhaseConfig)).run(userAnswers)

            result.value mustBe expectedResult
          }
        }
      }

      "when transition" - {
        val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
        when(mockPhaseConfig.phase).thenReturn(Phase.Transition)
        val bindingItinerary = arbitrary[Boolean].sample.value

        "when no security" in {

          val securityType = NoSecurityDetails

          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, securityType)
            .setValue(CountryOfDestinationPage, country)
            .setValue(OfficeOfDestinationPage, officeOfDestination)
            .setValue(BindingItineraryPage, bindingItinerary)

          val expectedResult = RoutingDomain(
            countryOfDestination = country,
            officeOfDestination = officeOfDestination,
            bindingItinerary = bindingItinerary,
            countriesOfRouting = Nil
          )

          val result: EitherType[RoutingDomain] = UserAnswersReader[RoutingDomain](RoutingDomain.userAnswersReader(mockPhaseConfig)).run(userAnswers)

          result.value mustBe expectedResult
        }

        "when there is security" in {

          val securityType = arbitrary[String](arbitrarySomeSecurityDetailsType).sample.value

          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, securityType)
            .setValue(CountryOfDestinationPage, country)
            .setValue(OfficeOfDestinationPage, officeOfDestination)
            .setValue(BindingItineraryPage, bindingItinerary)
            .setValue(CountryOfRoutingPage(index), country)

          val expectedResult = RoutingDomain(
            countryOfDestination = country,
            officeOfDestination = officeOfDestination,
            bindingItinerary = bindingItinerary,
            countriesOfRouting = Seq(
              CountryOfRoutingDomain(country)(index)
            )
          )

          val result: EitherType[RoutingDomain] = UserAnswersReader[RoutingDomain](RoutingDomain.userAnswersReader(mockPhaseConfig)).run(userAnswers)

          result.value mustBe expectedResult

        }
      }
    }

    "cannot be parsed from UserAnswers" - {

      "when country of destination page is missing" in {

        val securityType = arbitrary[String](arbitrarySecurityDetailsType).sample.value
        val userAnswers  = emptyUserAnswers.setValue(SecurityDetailsTypePage, securityType)

        val result: EitherType[RoutingDomain] = UserAnswersReader[RoutingDomain].run(userAnswers)

        result.left.value.page mustBe CountryOfDestinationPage
      }

      "when office of destination page is missing" in {

        val securityType = arbitrary[String](arbitrarySecurityDetailsType).sample.value
        val userAnswers = emptyUserAnswers
          .setValue(SecurityDetailsTypePage, securityType)
          .setValue(CountryOfDestinationPage, country)

        val result: EitherType[RoutingDomain] = UserAnswersReader[RoutingDomain].run(userAnswers)

        result.left.value.page mustBe OfficeOfDestinationPage
      }

      "when binding itinerary page is missing" in {

        val securityType = arbitrary[String](arbitrarySecurityDetailsType).sample.value
        val userAnswers = emptyUserAnswers
          .setValue(SecurityDetailsTypePage, securityType)
          .setValue(CountryOfDestinationPage, country)
          .setValue(OfficeOfDestinationPage, officeOfDestination)

        val result: EitherType[RoutingDomain] = UserAnswersReader[RoutingDomain].run(userAnswers)

        result.left.value.page mustBe BindingItineraryPage
      }

      "when post transition" - {
        val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
        when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

        "when add country page is missing" in {

          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(CountryOfDestinationPage, country)
            .setValue(OfficeOfDestinationPage, officeOfDestination)
            .setValue(BindingItineraryPage, false)

          val result: EitherType[RoutingDomain] = UserAnswersReader[RoutingDomain](RoutingDomain.userAnswersReader(mockPhaseConfig)).run(userAnswers)

          result.left.value.page mustBe AddCountryOfRoutingYesNoPage
        }

        "when binding itinerary is true and no countries added" in {

          val securityType = arbitrary[String](arbitrarySecurityDetailsType).sample.value
          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, securityType)
            .setValue(CountryOfDestinationPage, country)
            .setValue(OfficeOfDestinationPage, officeOfDestination)
            .setValue(BindingItineraryPage, true)

          val result: EitherType[RoutingDomain] = UserAnswersReader[RoutingDomain](RoutingDomain.userAnswersReader(mockPhaseConfig)).run(userAnswers)

          result.left.value.page mustBe CountryOfRoutingPage(Index(0))
        }

        "when add country is true and no countries added" in {

          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, NoSecurityDetails)
            .setValue(CountryOfDestinationPage, country)
            .setValue(OfficeOfDestinationPage, officeOfDestination)
            .setValue(BindingItineraryPage, false)
            .setValue(AddCountryOfRoutingYesNoPage, true)

          val result: EitherType[RoutingDomain] = UserAnswersReader[RoutingDomain](RoutingDomain.userAnswersReader(mockPhaseConfig)).run(userAnswers)

          result.left.value.page mustBe CountryOfRoutingPage(Index(0))
        }
      }

      "when there's security and no countries added" in {

        val securityType     = arbitrary[String](arbitrarySomeSecurityDetailsType).sample.value
        val bindingItinerary = arbitrary[Boolean].sample.value
        val userAnswers = emptyUserAnswers
          .setValue(SecurityDetailsTypePage, securityType)
          .setValue(CountryOfDestinationPage, country)
          .setValue(OfficeOfDestinationPage, officeOfDestination)
          .setValue(BindingItineraryPage, bindingItinerary)

        val result: EitherType[RoutingDomain] = UserAnswersReader[RoutingDomain].run(userAnswers)

        result.left.value.page mustBe CountryOfRoutingPage(Index(0))
      }
    }
  }
}
