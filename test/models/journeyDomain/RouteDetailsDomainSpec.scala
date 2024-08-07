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
import config.Constants.DeclarationType._
import config.Constants.SecurityType._
import config.PhaseConfig
import generators.Generators
import models.journeyDomain.exit.ExitDomain
import models.journeyDomain.locationOfGoods.LocationOfGoodsDomain
import models.journeyDomain.transit.TransitDomain
import models.reference.{CustomsOffice, SpecificCircumstanceIndicator}
import models.{Index, Phase, ProcedureType}
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.exit.AddCustomsOfficeOfExitYesNoPage
import pages.external._
import pages.locationOfGoods.{AddLocationOfGoodsPage, LocationTypePage}
import pages.transit.index.OfficeOfTransitInCL147Page
import pages.{AddSpecificCircumstanceIndicatorYesNoPage, SpecificCircumstanceIndicatorPage}

class RouteDetailsDomainSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  "RouteDetailsDomain" - {

    "specificCircumstanceIndicatorReader" - {
      "can be parsed from UserAnswers" - {
        "when security is 2 or 3 " in {
          forAll(
            Gen.oneOf(ExitSummaryDeclarationSecurityDetails, EntryAndExitSummaryDeclarationSecurityDetails),
            arbitrary[SpecificCircumstanceIndicator]
          ) {
            (securityDetailType, specificCircumstanceIndicator) =>
              val userAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, securityDetailType)
                .setValue(AddSpecificCircumstanceIndicatorYesNoPage, true)
                .setValue(SpecificCircumstanceIndicatorPage, specificCircumstanceIndicator)

              val result = UserAnswersReader[Option[SpecificCircumstanceIndicator]](
                RouteDetailsDomain.specificCircumstanceIndicatorReader.apply(Nil)
              ).run(userAnswers)

              result.value.value mustBe defined
          }
        }

        "when security is not 2 or 3 " in {

          forAll(Gen.oneOf(NoSecurityDetails, EntrySummaryDeclarationSecurityDetails)) {
            securityDetailType =>
              val userAnswers = emptyUserAnswers.setValue(SecurityDetailsTypePage, securityDetailType)

              val result = UserAnswersReader[Option[SpecificCircumstanceIndicator]](
                RouteDetailsDomain.specificCircumstanceIndicatorReader.apply(Nil)
              ).run(userAnswers)

              result.value.value must not be defined
              result.value.pages mustBe Nil
          }
        }
      }
    }

    "transitReader" - {
      "can be parsed from UserAnswers" - {
        "when TIR declaration type" in {
          val userAnswers = emptyUserAnswers.setValue(DeclarationTypePage, TIR)

          val result = UserAnswersReader[Option[TransitDomain]](
            RouteDetailsDomain.transitReader.apply(Nil)
          ).run(userAnswers)

          result.value.value must not be defined
          result.value.pages mustBe Nil
        }

        "when not a TIR declaration type" in {
          forAll(arbitrary[String](arbitraryNonTIRDeclarationType)) {
            declarationType =>
              val initialAnswers = emptyUserAnswers.setValue(DeclarationTypePage, declarationType)

              forAll(arbitraryTransitAnswers(initialAnswers)) {
                answers =>
                  val result = UserAnswersReader[Option[TransitDomain]](
                    RouteDetailsDomain.transitReader.apply(Nil)
                  ).run(answers)

                  result.value.value mustBe defined
              }
          }
        }
      }
    }

    "exitReader" - {
      "can be parsed from UserAnswers" - {
        "when security is in set {2,3}" - {
          val security = Gen.oneOf(ExitSummaryDeclarationSecurityDetails, EntryAndExitSummaryDeclarationSecurityDetails).sample.value

          "and at least one office of transit not in CL147" - {
            "and not adding offices of exit" in {

              val userAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, security)
                .setValue(OfficeOfTransitInCL147Page(Index(0)), false)
                .setValue(OfficeOfTransitInCL147Page(Index(1)), true)
                .setValue(AddCustomsOfficeOfExitYesNoPage, false)

              val result = UserAnswersReader[Option[ExitDomain]](
                RouteDetailsDomain.exitReader.apply(Nil)
              ).run(userAnswers)

              result.value.value must not be defined
              result.value.pages mustBe Seq(
                AddCustomsOfficeOfExitYesNoPage
              )
            }

            "and adding offices of exit" in {

              val initialAnswers = emptyUserAnswers
                .setValue(SecurityDetailsTypePage, security)
                .setValue(OfficeOfTransitInCL147Page(Index(0)), false)
                .setValue(OfficeOfTransitInCL147Page(Index(1)), false)
                .setValue(AddCustomsOfficeOfExitYesNoPage, true)

              forAll(
                arbitraryOfficeOfExitAnswers(initialAnswers, index)
              ) {
                answers =>
                  val result = UserAnswersReader[Option[ExitDomain]](
                    RouteDetailsDomain.exitReader.apply(Nil)
                  ).run(answers)

                  result.value.value mustBe defined
                  result.value.pages.head mustBe AddCustomsOfficeOfExitYesNoPage
              }
            }
          }

          "and all offices of transit in CL147" in {

            val userAnswers = emptyUserAnswers
              .setValue(SecurityDetailsTypePage, security)
              .setValue(OfficeOfTransitInCL147Page(Index(0)), true)
              .setValue(OfficeOfTransitInCL147Page(Index(1)), true)

            val result = UserAnswersReader[Option[ExitDomain]](
              RouteDetailsDomain.exitReader.apply(Nil)
            ).run(userAnswers)

            result.value.value must not be defined
            result.value.pages mustBe Nil
          }
        }

        "when security is not in set {2,3}" in {

          val security = Gen.oneOf(NoSecurityDetails, EntrySummaryDeclarationSecurityDetails).sample.value

          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, security)

          val result = UserAnswersReader[Option[ExitDomain]](
            RouteDetailsDomain.exitReader.apply(Nil)
          ).run(userAnswers)

          result.value.value must not be defined
          result.value.pages mustBe Nil
        }
      }
    }

    "cannot be parsed from user answers" - {
      "when security is in set {2,3}" - {
        val security = Gen.oneOf(ExitSummaryDeclarationSecurityDetails, EntryAndExitSummaryDeclarationSecurityDetails).sample.value

        "and at least one office of transit not in CL147" in {

          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, security)
            .setValue(OfficeOfTransitInCL147Page(Index(0)), false)
            .setValue(OfficeOfTransitInCL147Page(Index(1)), true)

          val result = UserAnswersReader[Option[ExitDomain]](
            RouteDetailsDomain.exitReader.apply(Nil)
          ).run(userAnswers)

          result.left.value.page mustBe AddCustomsOfficeOfExitYesNoPage
          result.left.value.pages mustBe Seq(
            AddCustomsOfficeOfExitYesNoPage
          )
        }

        "and no offices of transit in CL147" in {

          val userAnswers = emptyUserAnswers
            .setValue(SecurityDetailsTypePage, security)
            .setValue(OfficeOfTransitInCL147Page(Index(0)), false)
            .setValue(OfficeOfTransitInCL147Page(Index(1)), false)

          val result = UserAnswersReader[Option[ExitDomain]](
            RouteDetailsDomain.exitReader.apply(Nil)
          ).run(userAnswers)

          result.left.value.page mustBe AddCustomsOfficeOfExitYesNoPage
          result.left.value.pages mustBe Seq(
            AddCustomsOfficeOfExitYesNoPage
          )
        }
      }
    }

    "locationOfGoodsReader" - {
      "can be parsed from UserAnswers" - {
        "when post-transition" - {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

          "when additional declaration type is PreLodge (D)" - {
            "when procedure type is Normal" - {
              "and not adding a location of goods type" in {
                val userAnswers = emptyUserAnswers
                  .setValue(AdditionalDeclarationTypePage, "D")
                  .setValue(ProcedureTypePage, ProcedureType.Normal)
                  .setValue(AddLocationOfGoodsPage, false)

                val result = UserAnswersReader[Option[LocationOfGoodsDomain]](
                  RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig).apply(Nil)
                ).run(userAnswers)

                result.value.value must not be defined
                result.value.pages mustBe Seq(
                  AddLocationOfGoodsPage
                )
              }

              "and adding a location of goods type" in {
                val initialAnswers = emptyUserAnswers
                  .setValue(AdditionalDeclarationTypePage, "D")
                  .setValue(ProcedureTypePage, ProcedureType.Normal)
                  .setValue(AddLocationOfGoodsPage, true)

                forAll(arbitraryLocationOfGoodsAnswers(initialAnswers)) {
                  answers =>
                    val result = UserAnswersReader[Option[LocationOfGoodsDomain]](
                      RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig).apply(Nil)
                    ).run(answers)

                    result.value.value mustBe defined
                }
              }
            }

            "when procedure type is Simplified" - {
              "and adding a location of goods" in {
                val initialAnswers = emptyUserAnswers
                  .setValue(AdditionalDeclarationTypePage, "D")
                  .setValue(ProcedureTypePage, ProcedureType.Simplified)

                forAll(arbitraryLocationOfGoodsAnswers(initialAnswers)) {
                  answers =>
                    val result = UserAnswersReader[Option[LocationOfGoodsDomain]](
                      RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig).apply(Nil)
                    ).run(answers)

                    result.value.value mustBe defined
                }
              }
            }
          }

          "when additional declaration type is not PreLodge (D)" - {
            "when office of departure is in set CL147" - {
              "when procedure type is Normal" - {
                "and not adding a location of goods" in {
                  val customsOffice = arbitrary[CustomsOffice].sample.value

                  val userAnswers = emptyUserAnswers
                    .setValue(AdditionalDeclarationTypePage, "A")
                    .setValue(OfficeOfDeparturePage, customsOffice)
                    .setValue(OfficeOfDepartureInCL147Page, true)
                    .setValue(ProcedureTypePage, ProcedureType.Normal)
                    .setValue(AddLocationOfGoodsPage, false)

                  val result = UserAnswersReader[Option[LocationOfGoodsDomain]](
                    RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig).apply(Nil)
                  ).run(userAnswers)

                  result.value.value must not be defined
                  result.value.pages mustBe Seq(
                    AddLocationOfGoodsPage
                  )
                }

                "and adding a location of goods" in {
                  val customsOffice = arbitrary[CustomsOffice].sample.value

                  val initialAnswers = emptyUserAnswers
                    .setValue(AdditionalDeclarationTypePage, "A")
                    .setValue(OfficeOfDeparturePage, customsOffice)
                    .setValue(OfficeOfDepartureInCL147Page, true)
                    .setValue(ProcedureTypePage, ProcedureType.Normal)
                    .setValue(AddLocationOfGoodsPage, true)

                  forAll(arbitraryLocationOfGoodsAnswers(initialAnswers)) {
                    answers =>
                      val result = UserAnswersReader[Option[LocationOfGoodsDomain]](
                        RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig).apply(Nil)
                      ).run(answers)

                      result.value.value mustBe defined
                  }
                }
              }

              "when procedure type is Simplified" - {
                "and adding a location of goods" in {
                  val customsOffice = arbitrary[CustomsOffice].sample.value

                  val initialAnswers = emptyUserAnswers
                    .setValue(AdditionalDeclarationTypePage, "A")
                    .setValue(OfficeOfDeparturePage, customsOffice)
                    .setValue(OfficeOfDepartureInCL147Page, true)
                    .setValue(ProcedureTypePage, ProcedureType.Simplified)

                  forAll(arbitraryLocationOfGoodsAnswers(initialAnswers)) {
                    answers =>
                      val result = UserAnswersReader[Option[LocationOfGoodsDomain]](
                        RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig).apply(Nil)
                      ).run(answers)

                      result.value.value mustBe defined
                  }
                }
              }
            }

            "when office of departure is not in set CL147" - {
              "when procedure type is Normal" - {
                "and adding a location of goods" in {
                  val customsOffice = arbitrary[CustomsOffice].sample.value

                  val initialAnswers = emptyUserAnswers
                    .setValue(AdditionalDeclarationTypePage, "A")
                    .setValue(OfficeOfDeparturePage, customsOffice)
                    .setValue(OfficeOfDepartureInCL147Page, false)
                    .setValue(ProcedureTypePage, ProcedureType.Normal)

                  forAll(arbitraryLocationOfGoodsAnswers(initialAnswers)) {
                    answers =>
                      val result = UserAnswersReader[Option[LocationOfGoodsDomain]](
                        RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig).apply(Nil)
                      ).run(answers)

                      result.value.value mustBe defined
                  }
                }
              }

              "when procedure type is Simplified" - {
                "and adding a location of goods" in {
                  val customsOffice = arbitrary[CustomsOffice].sample.value

                  val initialAnswers = emptyUserAnswers
                    .setValue(AdditionalDeclarationTypePage, "A")
                    .setValue(OfficeOfDeparturePage, customsOffice)
                    .setValue(OfficeOfDepartureInCL147Page, false)
                    .setValue(ProcedureTypePage, ProcedureType.Simplified)

                  forAll(arbitraryLocationOfGoodsAnswers(initialAnswers)) {
                    answers =>
                      val result = UserAnswersReader[Option[LocationOfGoodsDomain]](
                        RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig).apply(Nil)
                      ).run(answers)

                      result.value.value mustBe defined
                  }
                }
              }
            }
          }
        }

        "when transition" - {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.Transition)

          "when simplified procedure" in {
            val initialAnswers = emptyUserAnswers
              .setValue(ProcedureTypePage, ProcedureType.Simplified)

            forAll(arbitraryLocationOfGoodsAnswers(initialAnswers)) {
              answers =>
                val result = UserAnswersReader[Option[LocationOfGoodsDomain]](
                  RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig).apply(Nil)
                ).run(answers)

                result.value.value mustBe defined
                result.value.pages.head must not be AddLocationOfGoodsPage
            }
          }

          "when normal procedure" in {
            val userAnswers = emptyUserAnswers
              .setValue(ProcedureTypePage, ProcedureType.Normal)
              .setValue(AddLocationOfGoodsPage, false)

            val result = UserAnswersReader[Option[LocationOfGoodsDomain]](
              RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig).apply(Nil)
            ).run(userAnswers)

            result.value.value must not be defined
            result.value.pages mustBe Seq(
              AddLocationOfGoodsPage
            )
          }
        }
      }

      "can not be parsed from user answers" - {
        "when transition" - {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.Transition)

          "and add location of goods type yes/no is unanswered" in {
            val userAnswers = emptyUserAnswers
              .setValue(ProcedureTypePage, ProcedureType.Normal)

            val result = UserAnswersReader[Option[LocationOfGoodsDomain]](
              RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig).apply(Nil)
            ).run(userAnswers)

            result.left.value.page mustBe AddLocationOfGoodsPage
            result.left.value.pages mustBe Seq(
              AddLocationOfGoodsPage
            )
          }
        }

        "when post-transition" - {
          val mockPhaseConfig: PhaseConfig = mock[PhaseConfig]
          when(mockPhaseConfig.phase).thenReturn(Phase.PostTransition)

          "when simplified procedure type" - {
            "and location type is unanswered" in {
              val userAnswers = emptyUserAnswers.setValue(ProcedureTypePage, ProcedureType.Simplified)

              val result = UserAnswersReader[Option[LocationOfGoodsDomain]](
                RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig).apply(Nil)
              ).run(userAnswers)

              result.left.value.page mustBe LocationTypePage
              result.left.value.pages mustBe Seq(
                LocationTypePage
              )
            }
          }

          "when normal procedure type" - {
            "when pre-lodging" - {
              "and add location of goods type yes/no is unanswered" in {
                val userAnswers = emptyUserAnswers
                  .setValue(ProcedureTypePage, ProcedureType.Normal)
                  .setValue(AdditionalDeclarationTypePage, "D")

                val result = UserAnswersReader[Option[LocationOfGoodsDomain]](
                  RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig).apply(Nil)
                ).run(userAnswers)

                result.left.value.page mustBe AddLocationOfGoodsPage
                result.left.value.pages mustBe Seq(
                  AddLocationOfGoodsPage
                )
              }
            }

            "when standard additional declaration type" - {
              "when office of departure is in set CL147" - {
                "and add location of goods type yes/no is unanswered" in {
                  val customsOffice = arbitrary[CustomsOffice].sample.value

                  val userAnswers = emptyUserAnswers
                    .setValue(ProcedureTypePage, ProcedureType.Normal)
                    .setValue(AdditionalDeclarationTypePage, "A")
                    .setValue(OfficeOfDeparturePage, customsOffice)
                    .setValue(OfficeOfDepartureInCL147Page, true)

                  val result = UserAnswersReader[Option[LocationOfGoodsDomain]](
                    RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig).apply(Nil)
                  ).run(userAnswers)

                  result.left.value.page mustBe AddLocationOfGoodsPage
                  result.left.value.pages mustBe Seq(
                    AddLocationOfGoodsPage
                  )
                }
              }

              "when office of departure is not in set CL147" - {
                "and location type is unanswered" in {
                  val customsOffice = arbitrary[CustomsOffice].sample.value

                  val userAnswers = emptyUserAnswers
                    .setValue(ProcedureTypePage, ProcedureType.Normal)
                    .setValue(AdditionalDeclarationTypePage, "A")
                    .setValue(OfficeOfDeparturePage, customsOffice)
                    .setValue(OfficeOfDepartureInCL147Page, false)

                  val result = UserAnswersReader[Option[LocationOfGoodsDomain]](
                    RouteDetailsDomain.locationOfGoodsReader(mockPhaseConfig).apply(Nil)
                  ).run(userAnswers)

                  result.left.value.page mustBe LocationTypePage
                  result.left.value.pages mustBe Seq(
                    LocationTypePage
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}
