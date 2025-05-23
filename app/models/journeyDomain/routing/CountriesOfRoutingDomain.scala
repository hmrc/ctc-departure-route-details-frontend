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

import config.Constants.SecurityType.*
import models.journeyDomain.*
import models.{Index, RichJsArray}
import pages.external.SecurityDetailsTypePage
import pages.routing.{AddCountryOfRoutingYesNoPage, BindingItineraryPage}
import pages.sections.Section
import pages.sections.routing.CountriesOfRoutingSection

case class CountriesOfRoutingDomain(
  countriesOfRouting: Seq[CountryOfRoutingDomain]
) extends JourneyDomainModel {

  override def page: Option[Section[?]] = countriesOfRouting match {
    case Nil => None
    case _   => Some(CountriesOfRoutingSection)
  }
}

object CountriesOfRoutingDomain {

  implicit def userAnswersReader: Read[CountriesOfRoutingDomain] = {
    lazy val arrayReader: Read[Seq[CountryOfRoutingDomain]] =
      CountriesOfRoutingSection.arrayReader.to {
        case x if x.isEmpty =>
          CountryOfRoutingDomain.userAnswersReader(Index(0)).toSeq
        case x =>
          x.traverse[CountryOfRoutingDomain](CountryOfRoutingDomain.userAnswersReader(_).apply(_))
      }

    lazy val emptyArrayReader: Read[CountriesOfRoutingDomain] =
      UserAnswersReader.emptyList[CountryOfRoutingDomain].map(CountriesOfRoutingDomain.apply)

    (
      SecurityDetailsTypePage.reader,
      BindingItineraryPage.reader
    ).to {
      case (NoSecurityDetails, false) =>
        AddCountryOfRoutingYesNoPage.reader.to {
          case true =>
            arrayReader.map(CountriesOfRoutingDomain.apply)
          case false =>
            emptyArrayReader
        }
      case _ => arrayReader.map(CountriesOfRoutingDomain.apply)
    }
  }
}
