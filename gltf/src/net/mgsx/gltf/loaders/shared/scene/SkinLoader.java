package net.mgsx.gltf.loaders.shared.scene;

import java.nio.FloatBuffer;

import com.badlogic.gdx.graphics.g3d.model.Node;
import com.badlogic.gdx.graphics.g3d.model.NodePart;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ArrayMap;

import net.mgsx.gltf.data.scene.GLTFNode;
import net.mgsx.gltf.data.scene.GLTFSkin;
import net.mgsx.gltf.loaders.exceptions.GLTFIllegalException;
import net.mgsx.gltf.loaders.shared.data.DataResolver;

public class SkinLoader {
	
	private int maxBones;

	public void load(Array<GLTFSkin> glSkins, Array<GLTFNode> glNodes, NodeResolver nodeResolver, DataResolver dataResolver) {
		if(glNodes != null){
			for(int i=0 ; i<glNodes.size ; i++){
				GLTFNode glNode = glNodes.get(i);
				if(glNode.skin != null){
					GLTFSkin glSkin = glSkins.get(glNode.skin);
					load(glSkin, glNode, nodeResolver.get(i), nodeResolver, dataResolver);
				}
			}
		}
	}

	private void load(GLTFSkin glSkin, GLTFNode glNode, Node node, NodeResolver nodeResolver, DataResolver dataResolver){
		
		Array<Matrix4> ibms = new Array<Matrix4>();
		Array<Integer> joints = new Array<Integer>();
		
		int bonesCount = glSkin.joints.size;
		maxBones = Math.max(maxBones, bonesCount);
		
		FloatBuffer floatBuffer = dataResolver.getBufferFloat(glSkin.inverseBindMatrices);
		
		for(int i=0 ; i<bonesCount ; i++){
			float [] matrixData = new float[16];
			floatBuffer.get(matrixData);
			ibms.add(new Matrix4(matrixData));
		}
		joints.addAll(glSkin.joints);
		
		if(ibms.size > 0){
			for(int i=0 ; i<node.parts.size ; i++){
				NodePart nodePart = node.parts.get(i);
				if(nodePart.bones != null){
					// special case when the same mesh is used by several skins.
					// in this case, we need to clone the node part
					NodePart newNodPart = new NodePart();
					newNodPart.material = nodePart.material;
					newNodPart.meshPart = nodePart.meshPart;
					node.parts.set(i, nodePart = newNodPart);
				}
				nodePart.bones = new Matrix4[ibms.size];
				nodePart.invBoneBindTransforms = new ArrayMap<Node, Matrix4>();
				for(int n=0 ; n<joints.size ; n++){
					nodePart.bones[n] = new Matrix4().idt();
					int nodeIndex = joints.get(n);
					Node key = nodeResolver.get(nodeIndex);
					if(key == null) throw new GLTFIllegalException("node not found for bone: " + nodeIndex);
					nodePart.invBoneBindTransforms.put(key, ibms.get(n));
				}
			}
		}
	}

	public int getMaxBones() {
		return maxBones;
	}

	
}
