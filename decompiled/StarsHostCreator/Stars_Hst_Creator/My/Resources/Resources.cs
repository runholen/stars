﻿namespace Stars_Hst_Creator.My.Resources
{
    using Microsoft.VisualBasic;
    using Microsoft.VisualBasic.CompilerServices;
    using System;
    using System.CodeDom.Compiler;
    using System.ComponentModel;
    using System.Diagnostics;
    using System.Globalization;
    using System.Resources;
    using System.Runtime.CompilerServices;

    [HideModuleName, GeneratedCode("System.Resources.Tools.StronglyTypedResourceBuilder", "2.0.0.0"), StandardModule, DebuggerNonUserCode, CompilerGenerated]
    internal sealed class Resources
    {
        private static CultureInfo resourceCulture;
        private static System.Resources.ResourceManager resourceMan;

        [EditorBrowsable(EditorBrowsableState.Advanced)]
        internal static CultureInfo Culture
        {
            get
            {
                return resourceCulture;
            }
            set
            {
                resourceCulture = value;
            }
        }

        [EditorBrowsable(EditorBrowsableState.Advanced)]
        internal static System.Resources.ResourceManager ResourceManager
        {
            get
            {
                if (object.ReferenceEquals(resourceMan, null))
                {
                    System.Resources.ResourceManager manager2 = new System.Resources.ResourceManager("Stars_Hst_Creator.Resources", typeof(Stars_Hst_Creator.My.Resources.Resources).Assembly);
                    resourceMan = manager2;
                }
                return resourceMan;
            }
        }
    }
}

