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

package services

import config.Constants.AuthorisedPlace
import connectors.ReferenceDataConnector
import models.LocationType
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class LocationTypeService @Inject() (
  referenceDataConnector: ReferenceDataConnector
)(implicit ec: ExecutionContext) {

  def getLocationTypes()(implicit hc: HeaderCarrier): Future[Seq[LocationType]] =
    referenceDataConnector
      .getTypesOfLocation()
      .map(filter)
      .map(_.sortBy(_.`type`.toLowerCase))

  def filter(typesOfLocation: Seq[LocationType]): Seq[LocationType] =
    typesOfLocation.filterNot(_.code == AuthorisedPlace)

}
