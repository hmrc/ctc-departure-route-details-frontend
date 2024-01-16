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
import models.domain._
import models.journeyDomain.ReaderSuccess
import models.{Index, RichJsArray}
import pages.external.SecurityDetailsTypePage
import pages.routing.{AddCountryOfRoutingYesNoPage, BindingItineraryPage}
import pages.sections.routing.CountriesOfRoutingSection

object CountriesOfRoutingDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[Seq[CountryOfRoutingDomain]] = {
    lazy val arrayReader: Read[Seq[CountryOfRoutingDomain]] = CountriesOfRoutingSection.arrayReader.apply(_).flatMap {
      case ReaderSuccess(x, pages) if x.isEmpty =>
        UserAnswersReader[CountryOfRoutingDomain](CountryOfRoutingDomain.userAnswersReader(Index(0))(pages)).map(_.toSeq)
      case ReaderSuccess(x, pages) =>
        x.traverse[CountryOfRoutingDomain](CountryOfRoutingDomain.userAnswersReader(_)(_)).apply(pages)
    }

    (
      SecurityDetailsTypePage.reader,
      BindingItineraryPage.reader
    ).tupleIt {
      case (NoSecurityDetails, _) if phaseConfig.phase == Transition => pages => UserAnswersReader.emptyList[CountryOfRoutingDomain].apply(pages)
      case (NoSecurityDetails, false) if phaseConfig.phase == PostTransition =>
        pages =>
          AddCountryOfRoutingYesNoPage.reader.apply(pages).flatMap {
            case ReaderSuccess(true, pages) =>
              arrayReader(pages)
            case ReaderSuccess(false, pages) =>
              UserAnswersReader.emptyList[CountryOfRoutingDomain].apply(pages)
          }
      case _ => pages => arrayReader(pages)
    }
  }
}
