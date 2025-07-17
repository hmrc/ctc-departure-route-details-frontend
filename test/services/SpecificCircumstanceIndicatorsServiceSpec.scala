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
import cats.data.NonEmptySet
import connectors.ReferenceDataConnector
import models.reference.SpecificCircumstanceIndicator
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, verify, when}
import org.scalatest.BeforeAndAfterEach

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SpecificCircumstanceIndicatorsServiceSpec extends SpecBase with BeforeAndAfterEach {

  private val mockRefDataConnector: ReferenceDataConnector = mock[ReferenceDataConnector]
  private val service                                      = new SpecificCircumstanceIndicatorsService(mockRefDataConnector)

  private val sci1                           = SpecificCircumstanceIndicator("A20", "Express consignments")
  private val sci2                           = SpecificCircumstanceIndicator("XXX", "Authorised Economic Operator")
  private val specificCircumstanceIndicators = NonEmptySet.of(sci1, sci2)

  override def beforeEach(): Unit = {
    reset(mockRefDataConnector)
    super.beforeEach()
  }

  "SpecificCircumstanceIndicatorsService" - {

    "getSpecificCircumstanceIndicators" - {
      "must return a list of sorted specific circumstance indicators" in {

        when(mockRefDataConnector.getSpecificCircumstanceIndicators()(any(), any()))
          .thenReturn(Future.successful(Right(specificCircumstanceIndicators)))

        service.getSpecificCircumstanceIndicators().futureValue mustEqual
          Seq(sci2, sci1)

        verify(mockRefDataConnector).getSpecificCircumstanceIndicators()(any(), any())
      }
    }
  }
}
