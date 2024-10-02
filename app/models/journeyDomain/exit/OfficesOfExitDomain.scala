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

package models.journeyDomain.exit

import models.journeyDomain._
import models.{Index, RichJsArray}
import pages.sections.Section
import pages.sections.exit.OfficesOfExitSection

case class OfficesOfExitDomain(
  officesOfExit: Seq[OfficeOfExitDomain]
) extends JourneyDomainModel {

  override def page: Option[Section[?]] = officesOfExit match {
    case Nil => None
    case _   => Some(OfficesOfExitSection)
  }
}

object OfficesOfExitDomain {

  implicit val userAnswersReader: Read[OfficesOfExitDomain] = {

    implicit def officesOfExitReader: Read[Seq[OfficeOfExitDomain]] =
      OfficesOfExitSection.arrayReader.to {
        case x if x.isEmpty =>
          OfficeOfExitDomain.userAnswersReader(Index(0)).toSeq
        case x =>
          x.traverse[OfficeOfExitDomain](OfficeOfExitDomain.userAnswersReader(_).apply(_))
      }

    officesOfExitReader.map(OfficesOfExitDomain.apply)
  }
}
