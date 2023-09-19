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

import connectors.ReferenceDataConnector
import models.{LocationOfGoodsIdentification, LocationType, UserAnswers}
import pages.locationOfGoods.LocationTypePage
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}
import services.LocationOfGoodsIdentificationTypeService._

class LocationOfGoodsIdentificationTypeService @Inject() (
  referenceDataConnector: ReferenceDataConnector
)(implicit ec: ExecutionContext) {

  def getLocationOfGoodsIdentificationTypes(userAnswers: UserAnswers)(implicit hc: HeaderCarrier): Future[Seq[LocationOfGoodsIdentification]] =
    referenceDataConnector
      .getQualifierOfTheIdentifications()
      .map(sort)
      .map {
        x => matchUserAnswers(userAnswers, x)
      }

  private def sort(locationOfGoodsIdentification: Seq[LocationOfGoodsIdentification]): Seq[LocationOfGoodsIdentification] =
    locationOfGoodsIdentification.sortBy(_.qualifier.toLowerCase)

}

object LocationOfGoodsIdentificationTypeService {

  def matchUserAnswers(userAnswers: UserAnswers, locationOfGoods: Seq[LocationOfGoodsIdentification]): Seq[LocationOfGoodsIdentification] =
    userAnswers.get(LocationTypePage) match {
      case Some(LocationType("A", _)) =>
        locationOfGoods.filter(
          x => x.qualifier == "V" || x.qualifier == "U"
        )
      case Some(LocationType("B", _)) =>
        locationOfGoods.filter(
          x => x.qualifier == "Y"
        )
      case Some(LocationType("C", _)) =>
        locationOfGoods.filter(
          x => x.qualifier == "X" || x.qualifier == "W" || x.qualifier == "U" || x.qualifier == "V"
        )
      case Some(LocationType("D", _)) =>
        locationOfGoods.filter(
          x => x.qualifier == "W" || x.qualifier == "U" || x.qualifier == "Z" || x.qualifier == "T"
        )
      case _ => locationOfGoods

    }
}
