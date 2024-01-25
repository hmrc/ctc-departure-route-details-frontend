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

import cats.implicits._
import config.Constants.SecurityType._
import config.PhaseConfig
import models.Phase.{PostTransition, Transition}
import models.journeyDomain._
import models.{Index, RichJsArray}
import pages.external.SecurityDetailsTypePage
import pages.routing.{AddCountryOfRoutingYesNoPage, BindingItineraryPage}
import pages.sections.Section
import pages.sections.routing.CountriesOfRoutingSection

case class CountriesOfRoutingDomain(
  countriesOfRouting: Seq[CountryOfRoutingDomain]
) extends JourneyDomainModel {

  override def page: Option[Section[_]] = countriesOfRouting match {
    case Nil => None
    case _   => Some(CountriesOfRoutingSection)
  }
}

object CountriesOfRoutingDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[CountriesOfRoutingDomain] = {
    lazy val arrayReader: Read[Seq[CountryOfRoutingDomain]] = CountriesOfRoutingSection.arrayReader.apply(_).flatMap {
      case ReaderSuccess(x, pages) if x.isEmpty =>
        CountryOfRoutingDomain.userAnswersReader(Index(0)).toSeq.apply(pages)
      case ReaderSuccess(x, pages) =>
        x.traverse[CountryOfRoutingDomain](CountryOfRoutingDomain.userAnswersReader(_).apply(_)).apply(pages)
    }

    lazy val emptyArrayReader: Read[CountriesOfRoutingDomain] =
      UserAnswersReader.emptyList[CountryOfRoutingDomain].map(CountriesOfRoutingDomain.apply)

    (
      SecurityDetailsTypePage.reader,
      BindingItineraryPage.reader
    ).apply {
      case (NoSecurityDetails, _) if phaseConfig.phase == Transition =>
        emptyArrayReader
      case (NoSecurityDetails, false) if phaseConfig.phase == PostTransition =>
        pages =>
          AddCountryOfRoutingYesNoPage.reader.apply(pages).flatMap {
            case ReaderSuccess(true, pages) =>
              arrayReader.map(CountriesOfRoutingDomain.apply).apply(pages)
            case ReaderSuccess(false, pages) =>
              emptyArrayReader.apply(pages)
          }
      case _ => pages => arrayReader.map(CountriesOfRoutingDomain.apply).apply(pages)
    }
  }
}
