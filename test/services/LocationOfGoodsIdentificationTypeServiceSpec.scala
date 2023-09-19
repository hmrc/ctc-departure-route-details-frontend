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

import base.SpecBase
import connectors.ReferenceDataConnector
import models.{Index, LocationOfGoodsIdentification}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import pages.sections.transit.OfficeOfTransitSection
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class LocationOfGoodsIdentificationServiceTypeSpec extends SpecBase with BeforeAndAfterEach {

  val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  val service                                      = new LocationOfGoodsIdentificationTypeService(mockRefDataConnector)

  private val id1 = LocationOfGoodsIdentification("U", "test1")
  private val id2 = LocationOfGoodsIdentification("V", "test2")
  private val ids = Seq(id1, id2)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "LocationOfGoodsIdentificationTypeService" - {

    "getLocationOfGoodsIdentificationTypes" - {
      "must return a list of sorted LocationOfGoodsIdentification" in {

        when(mockRefDataConnector.getQualifierOfTheIdentifications()(any(), any()))
          .thenReturn(Future.successful(ids))
        val answers = emptyUserAnswers.setValue(OfficeOfTransitSection(Index(0)), Json.obj("foo" -> "bar"))
        service.getLocationOfGoodsIdentificationTypes(answers).futureValue mustBe ids

      }
    }
  }
}
