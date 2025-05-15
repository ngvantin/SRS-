"use client"

import { useState, useEffect } from "react"
import { Button } from "@/components/ui/button"
import { Input } from "@/components/ui/input"
import { Card, CardHeader, CardTitle, CardContent } from "@/components/ui/card"
import { useRouter } from "next/navigation"
import { RefreshCw, Search, X, CheckCircle, List } from "lucide-react"
import { toast } from "@/components/ui/use-toast"
import { Role, Module} from "@/app/api/roles/api"
import { fetchRoleById, getAllModules, updateRolePermissions } from "@/app/api/roles/api"

export default function RolePermissionPage({ params }: { params: { id: string } }) {
  const router = useRouter()
  const [role, setRole] = useState<Role | null>(null)
  const [allModules, setAllModules] = useState<Module[]>([])
  const [assignedSearch, setAssignedSearch] = useState("")
  const [availableSearch, setAvailableSearch] = useState("")
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    const fetchData = async () => {
      try {
        console.log("Fetching data for role ID:", params.id)
        // Fetch role details
        const roleData = await fetchRoleById(Number(params.id))
        setRole(roleData)

        // Fetch all modules
        const modulesData = await getAllModules()
        setAllModules(modulesData)
      } catch (error) {
        toast({
          title: "Error",
          description: error instanceof Error ? error.message : "Failed to load data",
          variant: "destructive"
        })
      } finally {
        setLoading(false)
      }
    }

    fetchData()
  }, [params.id])

  const assignedModules = role?.modules || []
  const availableModules = allModules.filter(
    module => !assignedModules.some(m => m.id === module.id)
  )

  const filteredAssigned = assignedModules.filter(module =>
    module.name.toLowerCase().includes(assignedSearch.toLowerCase())
  )

  const filteredAvailable = availableModules.filter(module =>
    module.name.toLowerCase().includes(availableSearch.toLowerCase())
  )

  const handleAssign = (moduleId: number) => {
    if (!role) return
    setRole({
      ...role,
      modules: [...role.modules, allModules.find(m => m.id === moduleId)!]
    })
  }

  const handleUnassign = (moduleId: number) => {
    if (!role) return
    setRole({
      ...role,
      modules: role.modules.filter(m => m.id !== moduleId)
    })
  }

  const handleSave = async () => {
    if (!role) return
    setSaving(true)
    try {
      const res = await updateRolePermissions(Number(role.id), {
        id: Number(role.id),
        name: role.name,
        modules: role.modules.map(m => ({ id: m.id, name: m.name, path: m.path }))
      })

      if (!res) throw new Error("Failed to save permissions")

      toast({
        title: "Success",
        description: "Permissions updated successfully"
      })
    } catch (error) {
      toast({
        title: "Error",
        description: error instanceof Error ? error.message : "Failed to save permissions",
        variant: "destructive"
      })
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="container mx-auto py-12 flex justify-center">
        <RefreshCw className="h-8 w-8 animate-spin" />
      </div>
    )
  }

  if (!role) {
    return (
      <div className="container mx-auto py-12">
        <div className="bg-red-50 text-red-600 p-4 rounded-md">Role not found</div>
        <Button onClick={() => router.push("/roles")} className="mt-4">
          Back to Roles
        </Button>
      </div>
    )
  }

  return (
    <div className="container">
      <Card>
        <CardHeader>
          <CardTitle className="text-center">Permissions</CardTitle>
        </CardHeader>

        <CardContent className="p-3">
          <div className="mb-6">
            <h4 className="text-lg font-semibold mb-2">Role Information</h4>
            <div className="grid grid-cols-2 gap-4">
              <div>
                <p className="text-sm text-muted-foreground">Role Name</p>
                <p className="font-medium">{role.name}</p>
              </div>
              <div>
                <p className="text-sm text-muted-foreground">Assigned Modules</p>
                <p className="font-medium">{assignedModules.length}</p>
              </div>
            </div>
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {/* Assigned Modules */}
            <div>
              <Card>
                <CardHeader className="bg-gray-900 text-white flex flex-row items-center gap-2">
                  <CheckCircle className="h-5 w-5" />
                  <CardTitle>Assigned Modules</CardTitle>
                </CardHeader>
                <CardContent className="p-4">
                  <div className="relative mb-4">
                    <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                    <Input
                      placeholder="Search assigned modules..."
                      className="pl-10"
                      value={assignedSearch}
                      onChange={(e) => setAssignedSearch(e.target.value)}
                    />
                    {assignedSearch && (
                      <X 
                        className="absolute right-3 top-3 h-4 w-4 text-muted-foreground cursor-pointer"
                        onClick={() => setAssignedSearch("")}
                      />
                    )}
                  </div>

                  <div className="border rounded-md max-h-80 overflow-y-auto">
                    {filteredAssigned.length > 0 ? (
                      <div className="divide-y">
                        {filteredAssigned.map(module => (
                          <div 
                            key={module.id} 
                            className="p-3 hover:bg-gray-50 flex justify-between items-center cursor-pointer"
                            onClick={() => handleUnassign(module.id)}
                          >
                            <span>{module.name}</span>
                            <Button variant="ghost" size="sm">
                              <X className="h-4 w-4 text-red-500" />
                            </Button>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className="p-4 text-center text-muted-foreground">
                        {assignedModules.length === 0 
                          ? "No modules assigned" 
                          : "No matching modules found"}
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>
            </div>

            {/* Available Modules */}
            <div>
              <Card>
                <CardHeader className="bg-gray-900 text-white flex flex-row items-center gap-2">
                  <List className="h-5 w-5" />
                  <CardTitle>Available Modules</CardTitle>
                </CardHeader>
                <CardContent className="p-4">
                  <div className="relative mb-4">
                    <Search className="absolute left-3 top-3 h-4 w-4 text-muted-foreground" />
                    <Input
                      placeholder="Search available modules..."
                      className="pl-10"
                      value={availableSearch}
                      onChange={(e) => setAvailableSearch(e.target.value)}
                    />
                    {availableSearch && (
                      <X 
                        className="absolute right-3 top-3 h-4 w-4 text-muted-foreground cursor-pointer"
                        onClick={() => setAvailableSearch("")}
                      />
                    )}
                  </div>

                  <div className="border rounded-md max-h-80 overflow-y-auto">
                    {filteredAvailable.length > 0 ? (
                      <div className="divide-y">
                        {filteredAvailable.map(module => (
                          <div 
                            key={module.id} 
                            className="p-3 hover:bg-gray-50 flex justify-between items-center cursor-pointer"
                            onClick={() => handleAssign(module.id)}
                          >
                            <span>{module.name}</span>
                            <Button variant="ghost" size="sm">
                              <CheckCircle className="h-4 w-4 text-green-500" />
                            </Button>
                          </div>
                        ))}
                      </div>
                    ) : (
                      <div className="p-4 text-center text-muted-foreground">
                        {availableModules.length === 0 
                          ? "No modules available" 
                          : "No matching modules found"}
                      </div>
                    )}
                  </div>
                </CardContent>
              </Card>
            </div>
          </div>

          <div className="mt-6 flex justify-end gap-2">
            <Button onClick={() => router.push("/roles")}>
              Back to List
            </Button>
            <Button variant="outline" onClick={handleSave} disabled={saving}>
              {saving ? (
                <RefreshCw className="h-4 w-4 animate-spin mr-2" />
              ) : null}
              Save Changes
            </Button>
          </div>
        </CardContent>
      </Card>
    </div>
  )
}
