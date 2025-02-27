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

import config.Constants.CountryCode.*
import config.Constants.SecurityType.*
import models.journeyDomain.*
import models.reference.{Country, CustomsOffice}
import models.{DateTime, Index}
import pages.external.SecurityDetailsTypePage
import pages.routing.{OfficeOfDestinationInCL112Page, OfficeOfDestinationPage}
import pages.sections.Section
import pages.sections.transit.OfficeOfTransitSection
import pages.transit.index.*

case class OfficeOfTransitDomain(
  country: Option[Country],
  customsOffice: CustomsOffice,
  eta: Option[DateTime]
)(index: Index)
    extends JourneyDomainModel {

  override def page: Option[Section[?]] = Some(OfficeOfTransitSection(index))

  val label: String = country match {
    case Some(value) => s"$value - $customsOffice"
    case None        => customsOffice.toString
  }

}

object OfficeOfTransitDomain {

  // scalastyle:off cyclomatic.complexity
  // scalastyle:off method.length
  implicit def userAnswersReader(index: Index): Read[OfficeOfTransitDomain] = {

    lazy val etaReads: Read[Option[DateTime]] =
      SecurityDetailsTypePage.reader.to {
        case EntrySummaryDeclarationSecurityDetails | EntryAndExitSummaryDeclarationSecurityDetails =>
          OfficeOfTransitInCL147Page(index).reader.to {
            case true =>
              OfficeOfTransitETAPage(index).reader.toOption
            case false =>
              AddOfficeOfTransitETAYesNoPage(index).filterOptionalDependent(identity)(OfficeOfTransitETAPage(index).reader)
          }
        case _ =>
          AddOfficeOfTransitETAYesNoPage(index).filterOptionalDependent(identity)(OfficeOfTransitETAPage(index).reader)
      }

    lazy val readsWithoutCountry: Read[OfficeOfTransitDomain] =
      (
        OfficeOfTransitPage(index).reader,
        etaReads
      ).map(OfficeOfTransitDomain.apply(None, _, _)(index))

    lazy val readsWithCountry: Read[OfficeOfTransitDomain] =
      (
        UserAnswersReader.readInferred(OfficeOfTransitCountryPage(index), InferredOfficeOfTransitCountryPage(index)).toOption,
        OfficeOfTransitPage(index).reader,
        etaReads
      ).map(OfficeOfTransitDomain.apply(_, _, _)(index))

    lazy val reads: Read[OfficeOfTransitDomain] =
      OfficeOfDestinationPage.reader.to {
        _.countryId match {
          case AD => readsWithoutCountry
          case _  => readsWithCountry
        }
      }

    index.position match {
      case 0 =>
        OfficeOfDestinationInCL112Page.reader.to {
          case true  => readsWithoutCountry
          case false => reads
        }
      case _ => readsWithCountry
    }
  }
  // scalastyle:on cyclomatic.complexity
  // scalastyle:on method.length
}
