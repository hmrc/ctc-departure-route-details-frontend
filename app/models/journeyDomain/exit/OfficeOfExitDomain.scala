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

package models.journeyDomain.exit

import models.Index
import models.journeyDomain.{JourneyDomainModel, _}
import models.reference.{Country, CustomsOffice}
import pages.exit.index.{OfficeOfExitCountryPage, OfficeOfExitPage}
import pages.sections.Section
import pages.sections.exit.OfficeOfExitSection

case class OfficeOfExitDomain(
  country: Country,
  customsOffice: CustomsOffice
)(index: Index)
    extends JourneyDomainModel {

  override def page: Option[Section[?]] = Some(OfficeOfExitSection(index))

  val label: String = s"$country - $customsOffice"
}

object OfficeOfExitDomain {

  implicit def userAnswersReader(index: Index): Read[OfficeOfExitDomain] =
    (
      OfficeOfExitCountryPage(index).reader,
      OfficeOfExitPage(index).reader
    ).map(OfficeOfExitDomain.apply(_, _)(index))
}
