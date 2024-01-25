/*
 * Copyright 2024 HM Revenue & Customs
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

import config.PhaseConfig
import models.journeyDomain._
import models.{Index, RichJsArray}
import pages.sections.Section
import pages.sections.transit.OfficesOfTransitSection

case class OfficesOfTransitDomain(
  officesOfTransit: Seq[OfficeOfTransitDomain]
) extends JourneyDomainModel {

  override def page: Option[Section[_]] = officesOfTransit match {
    case Nil => None
    case _   => Some(OfficesOfTransitSection)
  }
}

object OfficesOfTransitDomain {

  implicit def userAnswersReader(implicit phaseConfig: PhaseConfig): Read[OfficesOfTransitDomain] = {

    implicit val officesOfTransitReader: Read[Seq[OfficeOfTransitDomain]] =
      OfficesOfTransitSection.arrayReader.apply(_).flatMap {
        case ReaderSuccess(x, pages) if x.isEmpty =>
          OfficeOfTransitDomain.userAnswersReader(Index(0)).toSeq.apply(pages)
        case ReaderSuccess(x, pages) =>
          x.traverse[OfficeOfTransitDomain](OfficeOfTransitDomain.userAnswersReader(_).apply(_)).apply(pages)
      }

    officesOfTransitReader.map(OfficesOfTransitDomain.apply)
  }
}
