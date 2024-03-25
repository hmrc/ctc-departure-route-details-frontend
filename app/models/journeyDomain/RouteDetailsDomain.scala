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

import config.Constants.AdditionalDeclarationType._
import config.Constants.DeclarationType._
import config.Constants.SecurityType._
import config.PhaseConfig
import models.Phase
import models.journeyDomain.exit.ExitDomain
import models.journeyDomain.loadingAndUnloading.LoadingAndUnloadingDomain
import models.journeyDomain.locationOfGoods.LocationOfGoodsDomain
import models.journeyDomain.routing.RoutingDomain
import models.journeyDomain.transit.TransitDomain
import models.reference.SpecificCircumstanceIndicator
import pages.exit.AddCustomsOfficeOfExitYesNoPage
import pages.external.{AdditionalDeclarationTypePage, DeclarationTypePage, OfficeOfDepartureInCL147Page, SecurityDetailsTypePage}
import pages.locationOfGoods.AddLocationOfGoodsPage
import pages.sections.routing.CountriesOfRoutingSection
import pages.sections.transit.OfficesOfTransitSection
import pages.sections.{RouteDetailsSection, Section}
import pages.{AddSpecificCircumstanceIndicatorYesNoPage, SpecificCircumstanceIndicatorPage}

case class RouteDetailsDomain(
  specificCircumstanceIndicator: Option[SpecificCircumstanceIndicator],
  routing: RoutingDomain,
  transit: Option[TransitDomain],
  exit: Option[ExitDomain],
  locationOfGoods: Option[LocationOfGoodsDomain],
  loadingAndUnloading: LoadingAndUnloadingDomain
) extends JourneyDomainModel {

  override def page: Option[Section[_]] = Some(RouteDetailsSection)
}

object RouteDetailsDomain {

  implicit val specificCircumstanceIndicatorReader: Read[Option[SpecificCircumstanceIndicator]] =
    SecurityDetailsTypePage.reader.to {
      case ExitSummaryDeclarationSecurityDetails | EntryAndExitSummaryDeclarationSecurityDetails =>
        AddSpecificCircumstanceIndicatorYesNoPage.filterOptionalDependent(identity)(SpecificCircumstanceIndicatorPage.reader)
      case _ =>
        UserAnswersReader.none
    }

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): UserAnswersReader[RouteDetailsDomain] =
    (
      specificCircumstanceIndicatorReader,
      RoutingDomain.userAnswersReader,
      transitReader
    ).to {
      case (specificCircumstanceIndicator, routing, transit) =>
        (
          Read(specificCircumstanceIndicator),
          Read(routing),
          Read(transit),
          exitReader(transit),
          locationOfGoodsReader,
          LoadingAndUnloadingDomain.userAnswersReader
        ).map(RouteDetailsDomain.apply)
    }.apply(Nil)

  implicit def transitReader(implicit phaseConfig: PhaseConfig): Read[Option[TransitDomain]] =
    DeclarationTypePage.reader.to {
      case TIR => UserAnswersReader.none
      case _   => TransitDomain.userAnswersReader.toOption
    }

  implicit def exitReader(transit: Option[TransitDomain])(implicit phaseConfig: PhaseConfig): Read[Option[ExitDomain]] =
    phaseConfig.phase match {
      case Phase.Transition =>
        (
          SecurityDetailsTypePage.reader,
          OfficesOfTransitSection.atLeastOneOfficeOfTransitIsInCL147
        ).to {
          case (ExitSummaryDeclarationSecurityDetails | EntryAndExitSummaryDeclarationSecurityDetails, false) =>
            AddCustomsOfficeOfExitYesNoPage.filterOptionalDependent(identity)(ExitDomain.userAnswersReader(_))
          case _ =>
            UserAnswersReader.none
        }
      case Phase.PostTransition =>
        (
          DeclarationTypePage.reader,
          SecurityDetailsTypePage.reader,
          CountriesOfRoutingSection.atLeastOneCountryOfRoutingIsInCL147
        ).to {
          case (declarationType, securityDetails, atLeastOneCountryOfRoutingInCL147) =>
            if (exitRequired(declarationType, securityDetails, atLeastOneCountryOfRoutingInCL147, transit)) {
              ExitDomain.userAnswersReader.toOption
            } else {
              (atLeastOneCountryOfRoutingInCL147, transit) match {
                case (true, Some(TransitDomain(_, list))) if list.nonEmpty =>
                  AddCustomsOfficeOfExitYesNoPage.filterOptionalDependent(identity)(ExitDomain.userAnswersReader(_))
                case _ =>
                  UserAnswersReader.none
              }
            }
        }
    }

  private def exitRequired(
    declarationType: String,
    securityDetails: String,
    atLeastOneCountryOfRoutingIsInCL147: Boolean,
    transit: Option[TransitDomain]
  ): Boolean =
    (declarationType, securityDetails, atLeastOneCountryOfRoutingIsInCL147, transit) match {
      case (TIR, _, _, _)                                                        => false
      case (_, NoSecurityDetails | EntrySummaryDeclarationSecurityDetails, _, _) => false
      case (_, _, true, Some(TransitDomain(_, list))) if list.nonEmpty           => false
      case _                                                                     => true
    }

  implicit def locationOfGoodsReader(implicit phaseConfig: PhaseConfig): Read[Option[LocationOfGoodsDomain]] = {
    lazy val optionalReader: Read[Option[LocationOfGoodsDomain]] =
      AddLocationOfGoodsPage.filterOptionalDependent(identity)(LocationOfGoodsDomain.userAnswersReader)

    phaseConfig.phase match {
      case Phase.Transition =>
        optionalReader
      case Phase.PostTransition =>
        AdditionalDeclarationTypePage.reader.to {
          case PreLodge =>
            optionalReader
          case _ =>
            OfficeOfDepartureInCL147Page.reader.to {
              case true  => optionalReader
              case false => LocationOfGoodsDomain.userAnswersReader.toOption
            }
        }
    }
  }
}
