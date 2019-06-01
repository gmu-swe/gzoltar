/*
 * Copied from pit - https://github.com/hcoles/pitest/
 *
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */
package com.gzoltar.internal.core.instr.analysis;

import java.util.Set;

public final class Block {
  private final int          firstInstruction;
  private final int          lastInstruction;
  private final Set<Integer> lines;

  public Block(final int firstInstruction, final int lastInstruction,
               final Set<Integer> lines) {
    this.firstInstruction = firstInstruction;
    this.lastInstruction = lastInstruction;
    this.lines = lines;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = (prime * result) + this.firstInstruction;
    result = (prime * result) + this.lastInstruction;
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    final Block other = (Block) obj;
    if (this.firstInstruction != other.firstInstruction) {
      return false;
    }
    if (this.lastInstruction != other.lastInstruction) {
      return false;
    }
    return true;
  }

  @Override
  public String toString() {
    return "Block [firstInstruction=" + this.firstInstruction
        + ", lastInstruction=" + this.lastInstruction + ", lines=" + this.lines + "]";
  }

  public boolean firstInstructionIs(final int ins) {
    return this.firstInstruction == ins;
  }

  public Set<Integer> getLines() {
    return this.lines;
  }

  public int getFirstInstruction() {
    return this.firstInstruction;
  }

  public int getLastInstruction() {
    return this.lastInstruction;
  }
}
