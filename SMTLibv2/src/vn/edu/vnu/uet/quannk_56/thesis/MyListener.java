/**
 * 
 */
package vn.edu.vnu.uet.quannk_56.thesis;

import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.report.PublisherExtension;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * @author wind
 *
 */
public class MyListener extends PropertyListenerAdapter implements
		PublisherExtension {
	public void instructionExecuted(VM vm, ThreadInfo currentThread,
			Instruction nextInstruction, Instruction executedInstruction) {
		if (executedInstruction.getSourceOrLocation().compareTo("null") != 0)
			System.out.println(executedInstruction.getSourceLine());
	}
}
