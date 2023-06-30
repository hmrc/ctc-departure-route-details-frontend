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

package config

import models.Phase
import models.Phase.{PostTransition, Transition}

trait PhaseConfig {
  val phase: Phase

  def amendMessageKey(key: String): String

  def lengthError(prefix: String): String = amendMessageKey(s"$prefix.error.length")

  val maxNumberAndStreetLength: Int
  val maxPostcodeLength: Int
  val locationMaxLength: Int
}

class TransitionConfig() extends PhaseConfig {
  override val phase: Phase = Transition

  override def amendMessageKey(key: String): String = s"$key.transition"

  override val maxNumberAndStreetLength: Int = 35
  override val maxPostcodeLength: Int        = 9
  override val locationMaxLength: Int        = 17
}

class PostTransitionConfig() extends PhaseConfig {
  override val phase: Phase = PostTransition

  override def amendMessageKey(key: String): String = s"$key.postTransition"

  override val maxNumberAndStreetLength: Int = 70
  override val maxPostcodeLength: Int        = 17
  override val locationMaxLength: Int        = 35
}
