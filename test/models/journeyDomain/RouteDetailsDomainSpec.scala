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

package models.journeyDomain

import base.SpecBase
import config.PhaseConfig
import generators.Generators
import models.DeclarationType.Option4
import models.SecurityDetailsType._
import models.domain.{EitherType, UserAnswersReader}
import models.journeyDomain.exit.ExitDomain
import models.journeyDomain.locationOfGoods.LocationOfGoodsDomain
import models.journeyDomain.transit.TransitDomain
import models.reference.{Country, CustomsOffice, SpecificCircumstanceIndicator}
import models.{DeclarationType, Phase, SecurityDetailsType}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.external.{DeclarationTypePage, OfficeOfDepartureInCL147Page, OfficeOfDeparturePage, SecurityDetailsTypePage}
import pages.locationOfGoods.AddLocationOfGoodsPage
import pages.routing.BindingItineraryPage
import pages.routing.index.{CountryOfRoutingInCL147Page, CountryOfRoutingPage}
import pages.{AddSpecificCircumstanceIndicatorYesNoPage, SpecificCircumstanceIndicatorPage}

class RouteDetailsDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "RouteDetailsDomain" - {

    "specificCircumstanceIndicatorReader" - {
      "can be parsed from UserAnswers" - {
        "when security is 2 or 3 " in {
          forAll(Gen.oneOf(ExitSummaryDeclarationSecurityDetails, EntryAndExitSummaryDeclarationSecurityDetails).sample.value,
                 arbitrary[SpecificCircumstanceIndicator]
          ) {
            (securityDetailType, specificCircumstanceIndicator) =>
              val userAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, securityDetailType)
                .setValue(AddSpecificCircumstanceIndicatorYesNoPage, true)
                .setValue(SpecificCircumstanceIndicatorPage, specificCircumstanceIndicator)

              val result: EitherType[Option[SpecificCircumstanceIndicator]] = UserAnswersReader[Option[SpecificCircumstanceIndicator]](
                RouteDetailsDomain.specificCircumstanceIndicatorReader
              ).run(userAnswers)

              result.value mustBe defined

          }
        }

        "when security is not 2 or 3 " in {

          forAll(Gen.oneOf(NoSecurityDetails, EntrySummaryDeclarationSecurityDetails).sample.value) {
            securityDetailType =>
              val userAnswers = emptyUserAnswers.setValue(SecurityDetailsTypePage, securityDetailType)

              val result: EitherType[Option[SpecificCircumstanceIndicator]] = UserAnswersReader[Option[SpecificCircumstanceIndicator]](
                RouteDetailsDomain.specificCircumstanceIndicatorReader
              ).run(userAnswers)

              result.value must not be defined

          }

        }
      }
    }

    "transitReader" - {
      "can be parsed from UserAnswers" - {
        "when TIR declaration type" in {
          val userAnswers = emptyUserAnswers.setValue(DeclarationTypePage, Option4)

          val result: EitherType[Option[TransitDomain]] = UserAnswersReader[Option[TransitDomain]](
            RouteDetailsDomain.transitReader
          ).run(userAnswers)

          result.value must not be defined
        }

        "when not a TIR declaration type" in {
          forAll(arbitrary[DeclarationType](arbitraryNonOption4DeclarationType)) {
            declarationType =>
              val initialAnswers = emptyUserAnswers.setValue(DeclarationTypePage, declarationType)

              forAll(arbitraryTransitAnswers(initialAnswers)) {
                answers =>
                  val result: EitherType[Option[TransitDomain]] = UserAnswersReader[Option[TransitDomain]](
                    RouteDetailsDomain.transitReader
                  ).run(answers)

                  result.value mustBe defined
              }
          }
        }
      }
    }

    "exitReader" - {
      "can be parsed from UserAnswers" - {
        "when TIR declaration type" in {

          forAll(arbitrary[SecurityDetailsType](arbitrarySomeSecurityDetailsType)) {
            security =>
              val userAnswers = emptyUserAnswers
                .setValue(DeclarationTypePage, Option4)
                .setValue(SecurityDetailsTypePage, security)

              val result: EitherType[Option[ExitDomain]] = UserAnswersReader[Option[ExitDomain]](
                RouteDetailsDomain.exitReader(None)
              ).run(userAnswers)

              result.value must not be defined
          }
        }

        "when not a TIR declaration type" - {
          val declarationType = arbitrary[DeclarationType](arbitraryNonOption4DeclarationType).sample.value

          "and security is in set {0,1}" in {
            val security = Gen.oneOf(NoSecurityDetails, EntrySummaryDeclarationSecurityDetails).sample.value

            val userAnswers = emptyUserAnswers
              .setValue(DeclarationTypePage, declarationType)
              .setValue(SecurityDetailsTypePage, security)

            val result: EitherType[Option[ExitDomain]] = UserAnswersReader[Option[ExitDomain]](
              RouteDetailsDomain.exitReader(None)
            ).run(userAnswers)

            result.value must not be defined
          }

          "and security is not in set {0,1}" - {
            val security = Gen.oneOf(ExitSummaryDeclarationSecurityDetails, EntryAndExitSummaryDeclarationSecurityDetails).sample.value

            "at least one of the countries of routing is not in set CL147 and office of transit is populated" - {
              "and office of transit answers have been provided" in {
                val answers = emptyUserAnswers
                  .setValue(DeclarationTypePage, declarationType)
                  .setValue(SecurityDetailsTypePage, security)
                  .setValue(BindingItineraryPage, true)
                  .setValue(CountryOfRoutingPage(index), arbitrary[Country].sample.value)
                  .setValue(CountryOfRoutingInCL147Page(index), false)

                forAll(arbitrary[Option[TransitDomain]](arbitraryPopulatedTransitDomain)) {
                  transit =>
                    val result: EitherType[Option[ExitDomain]] = UserAnswersReader[Option[ExitDomain]](
                      RouteDetailsDomain.exitReader(transit)
                    ).run(answers)

                    result.value must not be defined
                }
              }

              "and office of transit answers have not been provided" in {
                val initialAnswers = emptyUserAnswers
                  .setValue(DeclarationTypePage, declarationType)
                  .setValue(SecurityDetailsTypePage, security)
                  .setValue(BindingItineraryPage, true)
                  .setValue(CountryOfRoutingPage(index), arbitrary[Country].sample.value)
                  .setValue(CountryOfRoutingInCL147Page(index), false)

                forAll(
                  arbitraryOfficeOfExitAnswers(initialAnswers, index),
                  arbitrary[Option[TransitDomain]](arbitraryEmptyTransitDomain)
                ) {
                  (answers, transit) =>
                    val result: EitherType[Option[ExitDomain]] = UserAnswersReader[Option[ExitDomain]](
                      RouteDetailsDomain.exitReader(transit)
                    ).run(answers)

                    result.value mustBe defined
                }
              }
            }

            "and all of the countries of routing are in set CL147" in {
              val initialAnswers = emptyUserAnswers
                .setValue(DeclarationTypePage, declarationType)
                .setValue(SecurityDetailsTypePage, security)
                .setValue(BindingItineraryPage, true)
                .setValue(CountryOfRoutingPage(index), arbitrary[Country].sample.value)
                .setValue(CountryOfRoutingInCL147Page(index), true)

              forAll(arbitraryOfficeOfExitAnswers(initialAnswers, index)) {
                answers =>
                  val result: EitherType[Option[ExitDomain]] = UserAnswersReader[Option[ExitDomain]](
                    RouteDetailsDomain.exitReader(None)
                  ).run(answers)

                  result.value mustBe defined
              }
            }
          }
        }
      }
    }

    "locationOfGoodsReader" - {
      "can be parsed from UserAnswers" - {
        "when post-transition" - {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

          "when office of departure is in set CL147" - {
            val customsOffice = arbitrary[CustomsOffice].sample.value

            "and not adding a location of goods type" in {
              val userAnswers = emptyUserAnswers
                .setValue(OfficeOfDeparturePage, arbitrary[CustomsOffice].sample.value)
                .setValue(OfficeOfDepartureInCL147Page, true)
                .setValue(AddLocationOfGoodsPage, false)

              val result: EitherType[Option[LocationOfGoodsDomain]] = UserAnswersReader[Option[LocationOfGoodsDomain]](
                RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig)
              ).run(userAnswers)

              result.value must not be defined
            }

            "and adding a location of goods type" in {
              val initialAnswers = emptyUserAnswers
                .setValue(OfficeOfDeparturePage, customsOffice)
                .setValue(OfficeOfDepartureInCL147Page, true)
                .setValue(AddLocationOfGoodsPage, true)

              forAll(arbitraryLocationOfGoodsAnswers(initialAnswers)) {
                answers =>
                  val result: EitherType[Option[LocationOfGoodsDomain]] = UserAnswersReader[Option[LocationOfGoodsDomain]](
                    RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig)
                  ).run(answers)

                  result.value mustBe defined
              }
            }
          }

          "when office of departure is not in set CL147" in {
            val customsOffice = arbitrary[CustomsOffice].sample.value

            val initialAnswers = emptyUserAnswers
              .setValue(OfficeOfDeparturePage, customsOffice)
              .setValue(OfficeOfDepartureInCL147Page, false)

            forAll(arbitraryLocationOfGoodsAnswers(initialAnswers)) {
              answers =>
                val result: EitherType[Option[LocationOfGoodsDomain]] = UserAnswersReader[Option[LocationOfGoodsDomain]](
                  RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig)
                ).run(answers)

                result.value mustBe defined
            }
          }
        }
      }

      "can not be parsed from user answers" - {
        "when transition" - {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.Transition)

          "and add location of goods type yes/no is unanswered" in {
            val result: EitherType[Option[LocationOfGoodsDomain]] = UserAnswersReader[Option[LocationOfGoodsDomain]](
              RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig)
            ).run(emptyUserAnswers)

            result.left.value.page mustBe AddLocationOfGoodsPage
          }
        }
      }
    }
  }
}
